package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response with beneficiary account information.
 */
public record GetAccountResponse(
    UUID beneficiaryId,
    String beneficiaryNumber,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String email,
    String phoneNumber,
    String address,
    boolean inCouple,
    int numberOfDependents,
    double monthlyIncome,
    String iban,
    LocalDate registrationDate,
    String status
) implements Message.Response {}

