package cy.cav.protocol.demandes;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to create a new allocation demand.
 * Sent from client to GestionnaireDemandes (service).
 */
public record CreateDemandeRequest(
    UUID allocataireId,
    String allocationType  // RSA, ARE, APL, PRIME_ACTIVITE
) implements Message.Request<CreateDemandeResponse> {}

