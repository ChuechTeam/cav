package cy.cav.protocol;

import java.math.*;
import java.time.*;

public record BeneficiaryProfile(String beneficiaryNumber,
                                 String firstName,
                                 String lastName,
                                 LocalDate birthDate,
                                 String email,
                                 String phoneNumber,
                                 String address,
                                 boolean inCouple,
                                 int numberOfDependents,
                                 BigDecimal monthlyIncome,
                                 String iban,
                                 LocalDate registrationDate) {
}
