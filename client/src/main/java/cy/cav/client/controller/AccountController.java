package cy.cav.client.controller;

import cy.cav.client.dto.AllocataireDTO;
import cy.cav.client.dto.AllocataireResponse;
import cy.cav.framework.*;
import cy.cav.protocol.KnownActors;
import cy.cav.protocol.accounts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// REST controller for allocataire accounts (gestion des comptes allocataires)
@RestController
@RequestMapping("/api/comptes")
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    
    private final World world;
    
    public AccountController(World world) {
        this.world = world;
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
            
            // Get proxy actor address
            ActorAddress proxyAddress = getProxyAddress();
            
            // Send request to proxy (which forwards to service)
            CreateAccountResponse response = world.querySync(proxyAddress, request);
            
            log.info("Compte créé: {} (ID: {})", response.allocataireNumber(), response.allocataireId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new AllocataireResponse(
                    response.allocataireId(),
                    response.allocataireNumber(),
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
            
            // Get proxy actor address
            ActorAddress proxyAddress = getProxyAddress();
            
            // Send request to proxy (which forwards to service)
            GetAccountResponse response = world.querySync(proxyAddress, request);
            
            return ResponseEntity.ok(
                new AllocataireResponse(
                    response.allocataireId(),
                    response.allocataireNumber(),
                    response.registrationDate(),
                    response.status()
                )
            );
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du compte: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    //Récupère l'adresse de l'acteur AllocataireProxy

    private ActorAddress getProxyAddress() {
        // The proxy is spawned in the client's world with GESTIONNAIRE_COMPTE ID
        return world.server().address(KnownActors.GESTIONNAIRE_COMPTE);
    }
}

