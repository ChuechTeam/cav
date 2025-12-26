package cy.cav.client.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cy.cav.client.ServiceAPI;
import cy.cav.client.dto.AllocataireDTO;
import cy.cav.framework.ActorAddress;
import cy.cav.framework.ActorNotFoundException;
import cy.cav.framework.World;
import cy.cav.protocol.AllowancePrevision;
import cy.cav.protocol.AllowanceType;
import cy.cav.protocol.BeneficiaryProfile;
import cy.cav.protocol.Payment;
import cy.cav.protocol.accounts.CreateAccountRequest;
import cy.cav.protocol.accounts.CreateAccountResponse;
import cy.cav.protocol.accounts.GetAccountRequest;
import cy.cav.protocol.accounts.GetAccountResponse;

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
                dto.hasHousing(),
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
                    new AccountRepr(response.profile(), response.payments(), response.allowancePrevisions(), response.currentMonth())
            );
        } catch (ActorNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // todo: see if we should separate profile from api
    public record AccountRepr(BeneficiaryProfile profile,
                              List<Payment> payments,
                              Map<AllowanceType, AllowancePrevision> allowancePrevisions,
                              java.time.LocalDate currentMonth) { }

    public record CreatedAccountResponse(ActorAddress beneficiaryAddress) { }
}

