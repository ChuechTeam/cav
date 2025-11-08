package cy.cav.service;

import cy.cav.framework.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
class SendController {
    private final World world;

    SendController(World world) { this.world = world; }

    @GetMapping("/send/{to}")
    ResponseEntity<?> send(@PathVariable String to) {
        world.send(null, ActorAddress.fromString(to), new SayHi());
        return ResponseEntity.ok().build();
    }
}
