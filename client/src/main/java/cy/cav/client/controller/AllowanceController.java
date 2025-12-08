package cy.cav.client.controller;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.protocol.requests.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

// REST controller for allowance requests
@RestController
@RequestMapping("/api/accounts/{addr}/requests")
public class AllowanceController {
    private static final Logger log = LoggerFactory.getLogger(AllowanceController.class);

    private final World world;

    public AllowanceController(World world) { this.world = world; }

    // Creates RSA allowance request
    @PostMapping("/{type}")
    public ResponseEntity<AllowanceRequestResponse> requestAllowance(@PathVariable AllowanceType type,
                                                                     @PathVariable ActorAddress addr) {
        log.info("Allowance request received for beneficiary: {}", addr);

        try {
            RequestAllowanceResponse response = world.querySync(addr, new RequestAllowanceRequest(type));

            AllowanceRequestResponse httpResponse = new AllowanceRequestResponse(response.message());
            if (response.success()) {
                return ResponseEntity.ok(httpResponse);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(httpResponse);
            }
        } catch (ActorNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // TODO: I want to not receive RSA anymore!

    public record AllowanceRequestResponse(
            String message
    ) {}
}
