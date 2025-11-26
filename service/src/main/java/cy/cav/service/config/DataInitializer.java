package cy.cav.service.config;

import cy.cav.service.domain.Beneficiary;
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
        if (store.countBeneficiaries() == 0) {
            log.info("Initializing test data...");
            createTestData();
            log.info("Test data created: {} beneficiaries", store.countBeneficiaries());
        } else {
            log.info("Data already present: {} beneficiaries", store.countBeneficiaries());
        }
    }
    
    // Beneficiaries for testing
    private void createTestData() {
        // Beneficiary 1: Single person, no income
        Beneficiary beneficiary1 = new Beneficiary(
            "Jean",
            "Dupont",
            LocalDate.of(1990, 5, 15),
            "jean.dupont@example.com",
            false,  // Célibataire
            0       // Pas de personnes à charge
        );
        beneficiary1.setPhoneNumber("0612345678");
        beneficiary1.setAddress("12 Rue de la Paix, 75001 Paris");
        beneficiary1.setMonthlyIncome(0.0);
        beneficiary1.setIban("FR7612345678901234567890123");
        store.saveBeneficiary(beneficiary1);
        
        // Beneficiary 2: In couple with 2 children
        Beneficiary beneficiary2 = new Beneficiary(
            "Marie",
            "Martin",
            LocalDate.of(1985, 8, 20),
            "marie.martin@example.com",
            true,   // En couple
            2       // 2 personnes à charge
        );
        beneficiary2.setPhoneNumber("0698765432");
        beneficiary2.setAddress("45 Avenue des Champs, 69001 Lyon");
        beneficiary2.setMonthlyIncome(500.0);  // Modest income
        beneficiary2.setIban("FR7698765432109876543210987");
        store.saveBeneficiary(beneficiary2);
        
        // Beneficiary 3: Single person with 1 child
        Beneficiary beneficiary3 = new Beneficiary(
            "Pierre",
            "Bernard",
            LocalDate.of(1992, 3, 10),
            "pierre.bernard@example.com",
            false,  // Célibataire
            1       // 1 personne à charge
        );
        beneficiary3.setPhoneNumber("0654321098");
        beneficiary3.setAddress("78 Boulevard Saint-Michel, 33000 Bordeaux");
        beneficiary3.setMonthlyIncome(800.0);
        beneficiary3.setIban("FR7654321098765432109876543");
        store.saveBeneficiary(beneficiary3);
        
        log.debug("Test beneficiaries created:");
        log.debug("  - {} {} (ID: {})", beneficiary1.getFirstName(), beneficiary1.getLastName(), beneficiary1.getId());
        log.debug("  - {} {} (ID: {})", beneficiary2.getFirstName(), beneficiary2.getLastName(), beneficiary2.getId());
        log.debug("  - {} {} (ID: {})", beneficiary3.getFirstName(), beneficiary3.getLastName(), beneficiary3.getId());
    }
}

