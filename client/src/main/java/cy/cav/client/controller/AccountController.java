package cy.cav.client.controller;

import cy.cav.client.domain.Allocataire;
import cy.cav.client.dto.AllocataireDTO;
import cy.cav.client.dto.AllocataireResponse;
import cy.cav.client.store.AllocataireStore;
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
    
    private final AllocataireStore store;
    
    public AccountController(AllocataireStore store) {
        this.store = store;
    }
    
    // Creates a new allocataire account (création d'un compte allocataire)
    @PostMapping
    public ResponseEntity<AllocataireResponse> createAccount(@RequestBody AllocataireDTO dto) {
        log.info("Création de compte pour: {} {}", dto.firstName(), dto.lastName());
        
        // Create allocataire from DTO
        Allocataire allocataire = new Allocataire(
            dto.firstName(),
            dto.lastName(),
            dto.birthDate(),
            dto.email(),
            dto.inCouple(),
            dto.numberOfDependents()
        );
        
        allocataire.setPhoneNumber(dto.phoneNumber());
        allocataire.setAddress(dto.address());
        allocataire.setMonthlyIncome(dto.monthlyIncome());
        allocataire.setIban(dto.iban());
        
        // Save to store
        store.saveAllocataire(allocataire);
        
        log.info("Compte créé: {} (ID: {})", allocataire.getAllocataireNumber(), allocataire.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            new AllocataireResponse(
                allocataire.getId(),
                allocataire.getAllocataireNumber(),
                allocataire.getRegistrationDate(),
                "ACTIF"
            )
        );
    }
    
    // Gets allocataire by ID (récupération d'un allocataire par ID)
    @GetMapping("/{id}")
    public ResponseEntity<AllocataireResponse> getAccount(@PathVariable UUID id) {
        return store.findAllocataireById(id)
            .map(allocataire -> ResponseEntity.ok(
                new AllocataireResponse(
                    allocataire.getId(),
                    allocataire.getAllocataireNumber(),
                    allocataire.getRegistrationDate(),
                    "ACTIF"
                )
            ))
            .orElse(ResponseEntity.notFound().build());
    }
}

