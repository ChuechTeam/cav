package cy.cav.protocol.allocations;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to calculate RSA allocation amount.
 * Sent from client to RSACalculator (service).
 */
public record CalculateRSARequest(
    UUID beneficiaryId,        // ID of the beneficiary
    double monthlyIncome,      // Monthly income
    int numberOfDependents,    // Number of dependents
    boolean inCouple,          // In couple or single
    boolean hasHousing         // Has housing (for housing deduction)
) implements Message.Request<DecisionAllocationResponse> {}

