package cy.cav.protocol.demandes;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to get a demande by ID.
 */
public record GetDemandeRequest(
    UUID demandeId
) implements Message.Request<GetDemandeResponse> {}

