package cy.cav.protocol.accounts;

import java.math.BigDecimal;
import java.time.LocalDate;

import cy.cav.framework.Message;

/**
 * Request to create a new beneficiary account.

 */
public record CreateAccountRequest(
    String firstName,
    String lastName,
    LocalDate birthDate,
    String email,
    String phoneNumber,
    String address,
    boolean hasHousing,
    boolean inCouple,
    int numberOfDependents,
    BigDecimal monthlyIncome,
    String iban
) implements Message.Request<CreateAccountResponse> {}

