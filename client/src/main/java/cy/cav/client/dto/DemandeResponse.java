package cy.cav.client.dto;

import java.time.LocalDate;
import java.util.UUID;

// Response after allocation request (réponse après demande d'allocation)
public record DemandeResponse(
    UUID demandeId,
    String status,
    UUID allocationId,
    Double monthlyAmount,
    String rejectionReason,
    LocalDate requestDate
) {}

