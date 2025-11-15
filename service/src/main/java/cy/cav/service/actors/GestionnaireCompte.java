package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.protocol.accounts.*;
import cy.cav.service.domain.Allocataire;
import cy.cav.service.store.AllocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * Actor that manages allocataire accounts.
 * Allocataire data is stored only in the service.
 * 
 * Acteur qui gère les comptes allocataires.
 * Les données des allocataires sont stockées uniquement dans le service.
 */
public class GestionnaireCompte extends Actor {
    private static final Logger log = LoggerFactory.getLogger(GestionnaireCompte.class);
    
    private final AllocationStore store;
    
    static final Router<GestionnaireCompte> router = new Router<GestionnaireCompte>()
        .route(CreateAccountRequest.class, GestionnaireCompte::createAccount)
        .route(GetAccountRequest.class, GestionnaireCompte::getAccount)
        .route(CheckAccountExistsRequest.class, GestionnaireCompte::checkAccountExists);
    
    public GestionnaireCompte(ActorInit init, AllocationStore store) {
        super(init);
        this.store = store;
    }
    
    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }
    
    /**
     * Creates a new allocataire account.
     * Crée un nouveau compte allocataire.
     */
    CreateAccountResponse createAccount(CreateAccountRequest request) {
        log.info("Création de compte pour: {} {}", request.firstName(), request.lastName());
        
        // Create allocataire
        Allocataire allocataire = new Allocataire(
            request.firstName(),
            request.lastName(),
            request.birthDate(),
            request.email(),
            request.inCouple(),
            request.numberOfDependents()
        );
        
        allocataire.setPhoneNumber(request.phoneNumber());
        allocataire.setAddress(request.address());
        allocataire.setMonthlyIncome(request.monthlyIncome());
        allocataire.setIban(request.iban());
        
        // Generate allocataire number and registration date (génère le numéro d'allocataire)
        LocalDate registrationDate = LocalDate.now();
        String allocataireNumber = generateAllocataireNumber();
        allocataire.setAllocataireNumber(allocataireNumber);
        allocataire.setRegistrationDate(registrationDate);
        
        // Save to store
        store.saveAllocataire(allocataire);
        
        log.info("Compte créé: {} (ID: {})", allocataireNumber, allocataire.getId());
        
        return new CreateAccountResponse(
            allocataire.getId(),
            allocataireNumber,
            registrationDate,
            "ACTIF"
        );
    }
    
    /**
     * Gets an allocataire account by ID.
     * Récupère un compte allocataire par ID.
     */
    GetAccountResponse getAccount(GetAccountRequest request) {
        log.debug("Récupération de compte: {}", request.allocataireId());
        
        return store.findAllocataireById(request.allocataireId())
            .map(allocataire -> {
                // Generate number if not set (for backward compatibility)
                String allocataireNumber = allocataire.getAllocataireNumber();
                if (allocataireNumber == null) {
                    allocataireNumber = generateAllocataireNumberFor(allocataire);
                    allocataire.setAllocataireNumber(allocataireNumber);
                }
                
                LocalDate registrationDate = allocataire.getRegistrationDate();
                if (registrationDate == null) {
                    registrationDate = LocalDate.now();
                    allocataire.setRegistrationDate(registrationDate);
                }
                
                return new GetAccountResponse(
                    allocataire.getId(),
                    allocataireNumber,
                    allocataire.getFirstName(),
                    allocataire.getLastName(),
                    allocataire.getBirthDate(),
                    allocataire.getEmail(),
                    allocataire.getPhoneNumber(),
                    allocataire.getAddress(),
                    allocataire.isInCouple(),
                    allocataire.getNumberOfDependents(),
                    allocataire.getMonthlyIncome(),
                    allocataire.getIban(),
                    registrationDate,
                    "ACTIF"
                );
            })
            .orElseThrow(() -> {
                log.warn("Compte non trouvé: {}", request.allocataireId());
                return new RuntimeException("Compte non trouvé");
            });
    }
    
    /**
     * Checks if an allocataire account exists.
     * Vérifie si un compte allocataire existe.
     */
    CheckAccountExistsResponse checkAccountExists(CheckAccountExistsRequest request) {
        boolean exists = store.findAllocataireById(request.allocataireId()).isPresent();
        log.debug("Vérification existence compte {}: {}", request.allocataireId(), exists);
        return new CheckAccountExistsResponse(exists);
    }
    
    /**
     * Generates a unique allocataire number.
     * Génère un numéro d'allocataire unique.
     */
    private String generateAllocataireNumber() {
        LocalDate now = LocalDate.now();
        return "CAV" + now.getYear() + 
               String.format("%02d", now.getMonthValue()) +
               String.format("%02d", now.getDayOfMonth()) +
               String.format("%03d", (int)(Math.random() * 1000));
    }
    
    /**
     * Generates allocataire number for an existing allocataire (deterministic).
     * Génère un numéro d'allocataire pour un allocataire existant (déterministe).
     */
    private String generateAllocataireNumberFor(Allocataire allocataire) {
        // For existing allocataires, use a deterministic approach based on ID
        // Pour les allocataires existants, utiliser une approche déterministe basée sur l'ID
        String idStr = allocataire.getId().toString().replace("-", "");
        return "CAV" + idStr.substring(0, Math.min(11, idStr.length()));
    }
}

