package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response after creating a beneficiary account.
 */
public record CreateAccountResponse(
    UUID beneficiaryId,
    String beneficiaryNumber,
    LocalDate registrationDate,
    String status
) implements Message.Response {}

