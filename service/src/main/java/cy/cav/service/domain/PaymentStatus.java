package cy.cav.service.domain;

/**
 * Payment status
 */
public enum PaymentStatus {
    SCHEDULED,   // Prévu mais pas encore effectué
    COMPLETED,   // Effectué avec succès
    FAILED,      // Échec du virement
    CANCELLED    // Annulé
}

