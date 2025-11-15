package cy.cav.protocol.demandes;

import cy.cav.framework.Message;

/**
 * Response after updating a demande.
 */
public record UpdateDemandeResponse(
    boolean success
) implements Message.Response {}

