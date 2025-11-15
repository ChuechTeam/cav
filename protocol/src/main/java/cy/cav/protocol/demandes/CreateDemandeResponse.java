package cy.cav.protocol.demandes;

import cy.cav.framework.Message;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response after creating a demande.
 */
public record CreateDemandeResponse(
    UUID demandeId,
    UUID allocataireId,
    String allocationType,
    LocalDate requestDate,
    String status
) implements Message.Response {}

