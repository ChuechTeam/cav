package cy.cav.client;

import cy.cav.framework.*;
import cy.cav.protocol.KnownActors;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.requests.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * remplace acteur AllocataireProxy (simpler @Component)
 */
@Component
public class ServiceAPI {
    private static final Logger log = LoggerFactory.getLogger(ServiceAPI.class);
    
    private final World world;
    private final Network network;
    
    public ServiceAPI(World world, Network network) {
        this.world = world;
        this.network = network;
    }
    
    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        return forward(request, KnownActors.PREFECTURE);
    }

    private <T extends Message.Response> T forward(Message.Request<T> req, long actorId) {
        Server server = findServiceServer();
        if (server == null) {
            throw new IllegalStateException("Service CAV not found. Available servers: " + 
                network.servers().values().stream()
                    .map(Server::appName)
                    .toList());
        }
        
        ActorAddress actorAddress = server.address(actorId);
        log.debug("Forwarding request {} to actor {} on server {}", 
            req.getClass().getSimpleName(), actorId, server.appName());

        try {
            return world.querySync(actorAddress, req);
        } catch (ActorNotFoundException e) {
            // We assume the actor always exists here because it's a well-known actor.
            throw new RuntimeException(e);
        }
    }
    
    private Server findServiceServer() {
        Map<Long, Server> servers = network.servers();
        
        if (servers.isEmpty()) {
            log.warn("No servers available in network. Service may not be started or registered in Eureka.");
            return null;
        }
        
        Optional<Server> serviceServer = servers.values().stream()
            .filter(server -> server.appName().equalsIgnoreCase("cav-service"))
            .findFirst();
        
        if (serviceServer.isEmpty()) {
            log.error("Service cav-service not found via Eureka. Available servers: {}", 
                servers.values().stream()
                    .map(Server::appName)
                    .toList());
        }
        
        return serviceServer.orElse(null);
    }
}

