package cy.cav.client.dto;

import java.time.LocalDate;
import java.util.UUID;

// Response after creating a beneficiary
public record AllocataireResponse(
    UUID id,
    String beneficiaryNumber,
    LocalDate registrationDate,
    String status
) {}

