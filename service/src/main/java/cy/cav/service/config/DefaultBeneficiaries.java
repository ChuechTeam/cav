package cy.cav.service.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import cy.cav.service.domain.Beneficiary;

/// Gives some test data
@Component
public class DefaultBeneficiaries {
    public List<Beneficiary> getDefaultBeneficiaries() {
        // Beneficiary 1: Single person, no income
        Beneficiary beneficiary1 = new Beneficiary(
                "Jean",
                "Dupont",
                LocalDate.of(1990, 5, 15),
                "jean.dupont@example.com",
                "0612345678",
                "12 Rue de la Paix, 75001 Paris",
                false,  // Pas de logement
                false,  // Célibataire
                0,      // Pas de personnes à charge
                BigDecimal.ZERO,
                "FR7612345678901234567890123",
                LocalDate.now()
        );

        Beneficiary beneficiary2 = new Beneficiary(
                "Marie",
                "Martin",
                LocalDate.of(1985, 8, 20),
                "marie.martin@example.com",
                "0698765432",
                "45 Avenue des Champs, 69001 Lyon",
                true,   // A un logement
                true,   // En couple
                2,      // 2 personnes à charge
                BigDecimal.valueOf(500.0),  // Modest income
                "FR7698765432109876543210987",
                LocalDate.now()
        );

        // Beneficiary 3: Single person with 1 child
        Beneficiary beneficiary3 = new Beneficiary(
                "Pierre",
                "Bernard",
                LocalDate.of(1992, 3, 10),
                "pierre.bernard@example.com",
                "0654321098",
                "78 Boulevard Saint-Michel, 33000 Bordeaux",
                true,   // A un logement
                false,  // Célibataire
                1,      // 1 personne à charge
                BigDecimal.valueOf(800.0),
                "FR7654321098765432109876543",
                LocalDate.now()
        );

        return List.of(beneficiary1, beneficiary2, beneficiary3);
    }
}

