package cy.cav.framework;

import com.netflix.appinfo.*;
import com.netflix.discovery.*;
import com.netflix.discovery.shared.*;
import org.apache.http.HttpHeaders;
import org.slf4j.*;
import org.springframework.beans.factory.*;
import org.springframework.boot.web.context.*;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.reactive.function.client.*;

import java.util.*;
import java.util.concurrent.*;

@Component
class OutsideSender {
    private static final Logger log = LoggerFactory.getLogger(OutsideSender.class);
    private final ObjectProvider<EurekaClient> eurekaClientProvider;
    private final Server server;
    private final WebClient webClient;

    private final Map<Long, String> serverMap = new ConcurrentHashMap<>();

    OutsideSender(ObjectProvider<EurekaClient> eurekaClientProvider, Server server) {
        this.eurekaClientProvider = eurekaClientProvider;
        this.server = server;
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void send(Envelope envelope) {
        String receiverUrl = serverMap.getOrDefault(envelope.receiver().serverId(), null);
        if (receiverUrl == null) {
            log.error("Failed to send envelope, can't find URL for receiver server id: {}", envelope);
            // todo: retry sending after a while then give up.
            return;
        }

        log.info("Sending envelope to server at URL {}: {}", receiverUrl, envelope);
        webClient.post()
                .uri(receiverUrl + (receiverUrl.endsWith("/") ? "mailbox" : "/mailbox"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(envelope)
                .retrieve()
                .toBodilessEntity()
                .subscribe(r -> {
                    if (r.getStatusCode().is2xxSuccessful()) {
                        log.info("Successfully sent envelope to external server at URL {}: {}", receiverUrl, envelope);
                    } else {
                        log.error("Failed to send envelope to external server at URL {} (status code {}): {}",
                                receiverUrl, r.getStatusCode(), envelope);
                    }
                }, e -> log.error("Failed to send envelope to external server at URL {}: {}",
                        receiverUrl, envelope, e));
    }

    @EventListener(WebServerInitializedEvent.class)
    private void onWebServerReady() {
        EurekaClient client = eurekaClientProvider.getObject();
        client.registerEventListener(e -> onEvent(e, client));
    }

    private void onEvent(EurekaEvent event, EurekaClient eurekaClient) {
        if (!(event instanceof CacheRefreshedEvent)) { return; }

        Application application = eurekaClient.getApplication(server.appName());
        if (application != null) {
            for (InstanceInfo instance : application.getInstances()) {
                long serverId = Long.parseUnsignedLong(instance.getInstanceId(), 16);

                if (serverId != server.id()) {
                    serverMap.put(serverId, instance.getHomePageUrl());
                }
                // TODO: Remove servers that are gone from Eureka. Right now it's not a big deal though.
            }
        }
    }
}
