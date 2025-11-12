package cy.cav.protocol.allocations;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Response after an allocation request
 * (Retourné par tous les calculateurs [RSA, ARE, APL, Prime])
 */
public record DecisionAllocationResponse(
    boolean accepted,          // Allocation acceptée ou refusée
    double monthlyAmount,      // Montant mensuel
    String rejectionReason,    // Raison du refus (null si accepté)
    UUID allocationId,         // ID de l'allocation créée (null si refusé)
    String allocationType       // Type d'allocation (RSA, ARE, APL, PRIME_ACTIVITE)
) implements Message.Response {}

