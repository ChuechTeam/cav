package cy.cav.service.config;

import cy.cav.service.domain.Beneficiary;
import org.springframework.stereotype.Component;

import java.math.*;
import java.time.LocalDate;
import java.util.*;

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
                false,  // Célibataire
                0       // Pas de personnes à charge
        );
        beneficiary1.setPhoneNumber("0612345678");
        beneficiary1.setAddress("12 Rue de la Paix, 75001 Paris");
        beneficiary1.setMonthlyIncome(BigDecimal.ZERO);
        beneficiary1.setIban("FR7612345678901234567890123");

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
        beneficiary2.setMonthlyIncome(BigDecimal.valueOf(500.0));  // Modest income
        beneficiary2.setIban("FR7698765432109876543210987");

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
        beneficiary3.setMonthlyIncome(BigDecimal.valueOf(800.0));
        beneficiary3.setIban("FR7654321098765432109876543");

        return List.of(beneficiary1, beneficiary2, beneficiary3);
    }
}

