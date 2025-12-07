package cy.cav.protocol.requests;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Notification sent to Beneficiary actor to request an allowance.
 * This notification triggers the full allowance request process on the server side.
 */
public record RequestAllowanceNotification(
    UUID requestId, // ID of the allowance request
    UUID beneficiaryId,
    String allowanceType,  // RSA, ARE, APL, PRIME_ACTIVITE
    double monthlyIncome,
    int numberOfDependents,
    boolean inCouple,
    boolean hasHousing
) implements Message.Notification {}

