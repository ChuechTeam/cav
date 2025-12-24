package cy.cav.protocol;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BeneficiaryProfile(String firstName,
                                 String lastName,
                                 LocalDate birthDate,
                                 String email,
                                 String phoneNumber,
                                 String address,
                                 boolean hasHousing,
                                 boolean inCouple,
                                 int numberOfDependents,
                                 BigDecimal monthlyIncome,
                                 String iban,
                                 LocalDate registrationDate) {
}
