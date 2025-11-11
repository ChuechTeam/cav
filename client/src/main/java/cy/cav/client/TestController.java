package cy.cav.client;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import jakarta.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
class TestController {
    private final Network network;
    private final World world;

    TestController(Network network, World world) {
        this.network = network;
        this.world = world;
    }

    @GetMapping("/query")
    ResponseEntity<?> query() {
        Server server = firstServer();
        if (server == null) {
            return ResponseEntity.notFound().build();
        }

        ActorAddress actor = server.address(KnownActors.GREETER);
        HelloRequest request = new HelloRequest("World");

        HelloResponse response = world.querySync(actor, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/servers")
    ResponseEntity<?> servers() {
        return ResponseEntity.ok(network.servers().values());
    }

    @Nullable
    Server firstServer() {
        for (Server value : network.servers().values()) {
            return value;
        }
        return null;
    }
}
