package cy.cav.client;

import cy.cav.framework.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permet les requêtes depuis le front React
class ActorController {
    private final Network network;
    private final World world;

    ActorController(Network network, World world) {
        this.network = network;
        this.world = world;
    }

    /**
     * Liste tous les serveurs disponibles sur le réseau
     * GET /api/servers
     */
    @GetMapping("/servers")
    ResponseEntity<List<ServerDTO>> getServers() {
        List<ServerDTO> servers = network.servers().values().stream()
                .map(server -> new ServerDTO(
                        server.idString(),
                        server.appName(),
                        server.url(),
                        server.metadata()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(servers);
    }

    /**
     * Récupère les informations du serveur local
     * GET /api/servers/local
     */
    @GetMapping("/servers/local")
    ResponseEntity<ServerDTO> getLocalServer() {
        Server localServer = world.server();
        ServerDTO dto = new ServerDTO(
                localServer.idString(),
                localServer.appName(),
                localServer.url(),
                localServer.metadata()
        );
        return ResponseEntity.ok(dto);
    }
}

/**
 * DTO pour représenter un serveur
 */
record ServerDTO(
        String id,
        String name,
        String url,
        Map<String, String> metadata
) {}
