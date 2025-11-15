package cy.cav.protocol.demandes;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to update a demande (after allocation decision).
 */
public record UpdateDemandeRequest(
    UUID demandeId,
    String status,  // ACCEPTED, REJECTED
    UUID allocationId,  // null if rejected
    Double monthlyAmount,  // null if rejected
    String rejectionReason  // null if accepted
) implements Message.Request<UpdateDemandeResponse> {}

