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

    private final Network network;
    private final WebClient webClient; // Allows us to do run requests in a callback fashion

    OutsideSender(Network network) {
        // We need to use ObjectProvider cause otherwise EurekaClient is completely broken
        // and registers with port 0??
        this.network = network;
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /// Sends the envelope destined to an outside actor on the network.
    ///
    /// May not succeed due to network errors.
    public void send(Envelope<?> envelope) {
        // Find the URL of the server this envelope should be sent to.
        Server receiver = network.servers().getOrDefault(envelope.receiver().serverId(), null);
        String receiverUrl = receiver != null ? receiver.url() : null;

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
}
