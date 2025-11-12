package cy.cav.client.dto;

import java.util.UUID;

// DTO for RSA allocation request (demande d'allocation RSA)
public record DemandeRSADTO(
    UUID allocataireId,
    double monthlyIncome,
    int numberOfDependents,
    boolean inCouple,
    boolean hasHousing
) {}

