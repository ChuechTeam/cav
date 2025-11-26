package cy.cav.protocol.requests;

import cy.cav.framework.Message;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response with allowance request information.
 */
public record GetAllowanceRequestResponse(
    UUID requestId,
    UUID beneficiaryId,
    String allowanceType,
    LocalDate requestDate,
    String status,
    UUID allowanceId,
    Double monthlyAmount,
    String rejectionReason
) implements Message.Response {}

