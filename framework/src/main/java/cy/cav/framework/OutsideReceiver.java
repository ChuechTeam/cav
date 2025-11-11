package cy.cav.framework;

import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/// Receives envelopes from the network using an HTTP endpoint available at path `/mailbox`
@RestController
class OutsideReceiver {
    private static final Logger log = LoggerFactory.getLogger(OutsideReceiver.class);
    private final World world;

    OutsideReceiver(World world) {
        this.world = world;
    }

    @PostMapping("/mailbox")
    ResponseEntity<?> receive(@RequestBody Envelope<Message> envelope, HttpServletRequest httpServletRequest) {
        // Make sure this envelope is destined to this server.
        if (world.server().id() != envelope.receiver().serverId()) {
            log.warn("Received invalid envelope from host {} with a wrong server id: {}",
                    httpServletRequest.getRemoteAddr(), envelope);

            return ResponseEntity.badRequest().body("Wrong server id!");
        }

        // Then simply give the envelope to the world!
        log.info("Received envelope from host {}: {}", httpServletRequest.getRemoteAddr(), envelope);
        world.receive(envelope);

        return ResponseEntity.ok().build();
    }
}
