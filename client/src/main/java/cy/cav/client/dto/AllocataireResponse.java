package cy.cav.client.dto;

import java.time.LocalDate;
import java.util.UUID;

// Response after creating an allocataire (réponse après création d'allocataire)
public record AllocataireResponse(
    UUID id,
    String allocataireNumber,
    LocalDate registrationDate,
    String status
) {}

