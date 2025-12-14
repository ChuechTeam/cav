package cy.cav.client.controller;

import cy.cav.client.*;
import cy.cav.client.dto.*;
import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.protocol.accounts.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// REST controller for allocataire accounts (gestion des comptes allocataires)
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final World world;
    private final ServiceAPI serviceAPI;

    public AccountController(World world, ServiceAPI serviceAPI) {
        this.world = world;
        this.serviceAPI = serviceAPI;
    }

    // Creates a new allocataire account (création d'un compte allocataire)
    @PostMapping
    public ResponseEntity<CreatedAccountResponse> createAccount(@RequestBody AllocataireDTO dto) {
        // Create request message
        CreateAccountRequest request = new CreateAccountRequest(
                dto.firstName(),
                dto.lastName(),
                dto.birthDate(),
                dto.email(),
                dto.phoneNumber(),
                dto.address(),
                dto.inCouple(),
                dto.numberOfDependents(),
                dto.monthlyIncome(),
                dto.iban()
        );

        // Send request via ServiceAPI (which forwards to service)
        CreateAccountResponse response = serviceAPI.createAccount(request);

        log.info("Account created: {}", response.beneficiaryAddress());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CreatedAccountResponse(
                        response.beneficiaryAddress()
                )
        );
    }

    // Gets allocataire data
    @GetMapping("/{addr}")
    public ResponseEntity<?> getAccount(@PathVariable ActorAddress addr) {
        try {
            GetAccountResponse response = world.querySync(addr, new GetAccountRequest());

            return ResponseEntity.ok(
                    new AccountRepr(response.profile(), response.payments(), response.allowancePrevisions())
            );
        } catch (ActorNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // todo: see if we should separate profile from api
    public record AccountRepr(BeneficiaryProfile profile,
                              List<Payment> payments,
                              Map<AllowanceType, AllowancePrevision> allowancePrevisions) { }

    public record CreatedAccountResponse(ActorAddress beneficiaryAddress) { }
}

