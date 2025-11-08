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

/// Sends messages to outside actors on the network, using Eureka to find servers.
///
/// Used internally by the framework in [World].
///
/// Messages are sent to the `/mailbox` endpoint of other servers, which [OutsideReceiver] receives.
///
/// @see OutsideReceiver
@Component
class OutsideSender {
    private static final Logger log = LoggerFactory.getLogger(OutsideSender.class);

    private final ObjectProvider<EurekaClient> eurekaClientProvider;
    private final Server server;
    private final WebClient webClient; // Allows us to do run requests in a callback fashion

    private final Map<Long, String> serverMap = new ConcurrentHashMap<>();

    OutsideSender(ObjectProvider<EurekaClient> eurekaClientProvider, Server server) {
        // We need to use ObjectProvider cause otherwise EurekaClient is completely broken
        // and registers with port 0??
        this.eurekaClientProvider = eurekaClientProvider;
        this.server = server;
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /// Sends the envelope destined to an outside actor on the network.
    ///
    /// May not succeed due to network errors.
    public void send(Envelope<?> envelope) {
        // Find the URL of the server this envelope should be sent to.
        String receiverUrl = serverMap.getOrDefault(envelope.receiver().serverId(), null);
        if (receiverUrl == null) {
            log.error("Failed to send envelope, can't find URL for receiver server id: {}", envelope);
            // todo: retry sending after a while then give up. Hint: TaskScheduler
            return;
        }

        // Now send the envelope!
        log.info("Sending envelope to server at URL {}: {}", receiverUrl, envelope);
        webClient.post()
                .uri(receiverUrl + (receiverUrl.endsWith("/") ? "mailbox" : "/mailbox"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(envelope)
                .retrieve()
                .toBodilessEntity()
                .subscribe(r -> {
                    // We got a response from the OutsideReceiver controller!
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
        // Now we know our port, grab the eureka client and listen to its CacheRefreshedEvent
        // which triggers every few seconds.
        EurekaClient client = eurekaClientProvider.getObject();
        client.registerEventListener(e -> onEvent(e, client));
    }

    private void onEvent(EurekaEvent event, EurekaClient eurekaClient) {
        if (!(event instanceof CacheRefreshedEvent)) { return; }

        // The Eureka client has updated its cache, let's see which servers are available on the network and
        // update our local cache.
        Application application = eurekaClient.getApplication(server.appName());
        if (application == null) {
            // Shouldn't happen, but if it does it's just that Eureka's broken for a moment.
            return;
        }

        // Look over all registered servers.
        for (InstanceInfo instance : application.getInstances()) {
            // Because the instance id is a string, we need to convert it to a number.
            // Server ids are stored in hexadecimal format, so we need to indicate
            // that it's in a hexa (16) format.
            long serverId = Long.parseUnsignedLong(instance.getInstanceId(), 16);

            // Put the server in the map, and don't register ourselves, no need to.
            if (serverId != server.id()) {
                serverMap.put(serverId, instance.getHomePageUrl());
            }
            // TODO: Remove servers that are gone from Eureka. Right now it's not a big deal though.
        }
    }
}
