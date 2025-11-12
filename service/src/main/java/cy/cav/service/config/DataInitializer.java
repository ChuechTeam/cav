package cy.cav.service.config;

import cy.cav.service.domain.Allocataire;
import cy.cav.service.store.AllocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

//Initializes test data on application startup.

@Component
public class DataInitializer implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    private final AllocationStore store;
    
    public DataInitializer(AllocationStore store) {
        this.store = store;
    }
    
    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        // create data only if store is empty
        if (store.countAllocataires() == 0) {
            log.info("Initialisation des données de test...");
            createTestData();
            log.info("Données de test créées: {} allocataires", store.countAllocataires());
        } else {
            log.info("Données déjà présentes: {} allocataires", store.countAllocataires());
        }
    }
    
    // Allocataires for testing
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
        
        // Allocataire 3: Personne seule avec 1 enfant
        Allocataire alloc3 = new Allocataire(
            "Pierre",
            "Bernard",
            LocalDate.of(1992, 3, 10),
            "pierre.bernard@example.com",
            false,  // Célibataire
            1       // 1 personne à charge
        );
        alloc3.setPhoneNumber("0654321098");
        alloc3.setAddress("78 Boulevard Saint-Michel, 33000 Bordeaux");
        alloc3.setMonthlyIncome(800.0);
        alloc3.setIban("FR7654321098765432109876543");
        store.saveAllocataire(alloc3);
        
        log.debug("Allocataires de test créés:");
        log.debug("  - {} {} (ID: {})", alloc1.getFirstName(), alloc1.getLastName(), alloc1.getId());
        log.debug("  - {} {} (ID: {})", alloc2.getFirstName(), alloc2.getLastName(), alloc2.getId());
        log.debug("  - {} {} (ID: {})", alloc3.getFirstName(), alloc3.getLastName(), alloc3.getId());
    }
}

