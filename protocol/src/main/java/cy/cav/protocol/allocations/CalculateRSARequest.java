package cy.cav.protocol.allocations;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to calculate RSA allocation amount.
 * Sent from client to RSACalculator (service).
 */
public record CalculateRSARequest(
    UUID allocataireId,        // ID de l'allocataire
    double monthlyIncome,      // Revenus mensuels
    int numberOfDependents,    // Nombre de personnes à charge
    boolean inCouple,          // En couple ou célibataire
    boolean hasHousing         // A un logement (pour forfait logement)
) implements Message.Request<DecisionAllocationResponse> {}

