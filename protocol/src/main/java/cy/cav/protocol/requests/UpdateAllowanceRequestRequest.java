package cy.cav.protocol.requests;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to update an allowance request (after allowance decision).
 */
public record UpdateAllowanceRequestRequest(
    UUID requestId,
    String status,  // ACCEPTED, REJECTED
    UUID allowanceId,  // null if rejected
    Double monthlyAmount,  // null if rejected
    String rejectionReason  // null if accepted
) implements Message.Request<UpdateAllowanceRequestResponse> {}

