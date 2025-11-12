package cy.cav.service.domain;

/**
 * Status of an active allocation
 */
public enum AllocationStatus {
    ACTIVE,      // Allocation active, versements en cours
    SUSPENDED,   // Temporairement suspendue
    TERMINATED   // Terminée (plus de versements)
}

