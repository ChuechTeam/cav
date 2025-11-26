package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

import java.time.LocalDate;

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
    boolean inCouple,
    int numberOfDependents,
    double monthlyIncome,
    String iban
) implements Message.Request<CreateAccountResponse> {}

