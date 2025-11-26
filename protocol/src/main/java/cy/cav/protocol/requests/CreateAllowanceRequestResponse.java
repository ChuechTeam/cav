package cy.cav.protocol.requests;

import cy.cav.framework.Message;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response after creating an allowance request.
 */
public record CreateAllowanceRequestResponse(
    UUID requestId,
    UUID beneficiaryId,
    String allowanceType,
    LocalDate requestDate,
    String status
) implements Message.Response {}

