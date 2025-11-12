package cy.cav.client.config;

import cy.cav.client.domain.Allocataire;
import cy.cav.client.store.AllocataireStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

//Initializes test data on client application startup
// Crée des allocations de test

@Component
public class DataInitializer implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    private final AllocataireStore store;
    
    public DataInitializer(AllocataireStore store) {
        this.store = store;
    }
    
    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        // Ne créer des données que si le store est vide
        // Only create data if store is empty
        if (store.countAllocataires() == 0) {
            log.info("Initialisation des données de test côté client...");
            createTestData();
            log.info("Données de test créées: {} allocataires", store.countAllocataires());
        } else {
            log.info("Données déjà présentes: {} allocataires", store.countAllocataires());
        }
    }
    
    /**
     * Crée des allocataires de test.
     * Creates test allocataires.
     */
    private void createTestData() {
        // Allocataire 1: Personne seule, sans revenus
        Allocataire alloc1 = new Allocataire(
            "Jean",
            "Dupont",
            LocalDate.of(1990, 5, 15),
            "jean.dupont@example.com",
            false,  // Célibataire
            0       // Pas de personnes à charge
        );
        alloc1.setPhoneNumber("0612345678");
        alloc1.setAddress("12 Rue de la Paix, 75001 Paris");
        alloc1.setMonthlyIncome(0.0);
        alloc1.setIban("FR7612345678901234567890123");
        store.saveAllocataire(alloc1);
        
        // Allocataire 2: En couple avec 2 enfants
        Allocataire alloc2 = new Allocataire(
            "Marie",
            "Martin",
            LocalDate.of(1985, 8, 20),
            "marie.martin@example.com",
            true,   // En couple
            2       // 2 personnes à charge
        );
        alloc2.setPhoneNumber("0698765432");
        alloc2.setAddress("45 Avenue des Champs, 69001 Lyon");
        alloc2.setMonthlyIncome(500.0);  // Revenus modestes
        alloc2.setIban("FR7698765432109876543210987");
        store.saveAllocataire(alloc2);
        
        log.debug("Allocataires de test créés:");
        log.debug("  - {} {} (Numéro: {}, ID: {})", 
            alloc1.getFirstName(), alloc1.getLastName(), 
            alloc1.getAllocataireNumber(), alloc1.getId());
        log.debug("  - {} {} (Numéro: {}, ID: {})", 
            alloc2.getFirstName(), alloc2.getLastName(), 
            alloc2.getAllocataireNumber(), alloc2.getId());
    }
}

