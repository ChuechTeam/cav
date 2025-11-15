package cy.cav.protocol.demandes;

import cy.cav.framework.Message;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response with demande information.
 */
public record GetDemandeResponse(
    UUID demandeId,
    UUID allocataireId,
    String allocationType,
    LocalDate requestDate,
    String status,
    UUID allocationId,
    Double monthlyAmount,
    String rejectionReason
) implements Message.Response {}

