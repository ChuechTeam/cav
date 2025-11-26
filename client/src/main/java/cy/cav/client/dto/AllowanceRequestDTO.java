package cy.cav.client.dto;

import java.util.UUID;

// DTO for creating an allowance request (RSA)
public record AllowanceRequestDTO(
    UUID beneficiaryId,
    double monthlyIncome,
    int numberOfDependents,
    boolean inCouple,
    boolean hasHousing
) {}

