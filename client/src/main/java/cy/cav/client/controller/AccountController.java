package cy.cav.client.controller;

import cy.cav.client.ServiceAPI;
import cy.cav.client.dto.AllocataireDTO;
import cy.cav.client.dto.AllocataireResponse;
import cy.cav.protocol.accounts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// REST controller for allocataire accounts (gestion des comptes allocataires)
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    
    private final ServiceAPI serviceAPI;
    
    public AccountController(ServiceAPI serviceAPI) {
        this.serviceAPI = serviceAPI;
    }
    
    // Creates a new allocataire account (création d'un compte allocataire)
    @PostMapping
    public ResponseEntity<AllocataireResponse> createAccount(@RequestBody AllocataireDTO dto) {
        log.info("Création de compte pour: {} {}", dto.firstName(), dto.lastName());
        
        try {
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
            
            log.info("Compte créé: {} (ID: {})", response.beneficiaryNumber(), response.beneficiaryId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new AllocataireResponse(
                    response.beneficiaryId(),
                    response.beneficiaryNumber(),
                    response.registrationDate(),
                    response.status()
                )
            );
            
        } catch (Exception e) {
            log.error("Erreur lors de la création du compte", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Gets allocataire by ID (récupération d'un allocataire par ID)
    @GetMapping("/{id}")
    public ResponseEntity<AllocataireResponse> getAccount(@PathVariable UUID id) {
        try {
            // Create request message
            GetAccountRequest request = new GetAccountRequest(id);
            
            // Send request via ServiceAPI (which forwards to service)
            GetAccountResponse response = serviceAPI.getAccount(request);
            
            return ResponseEntity.ok(
                new AllocataireResponse(
                    response.beneficiaryId(),
                    response.beneficiaryNumber(),
                    response.registrationDate(),
                    response.status()
                )
            );
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du compte: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
}

