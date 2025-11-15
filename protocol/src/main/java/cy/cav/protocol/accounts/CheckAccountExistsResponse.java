package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

/**
 * Response indicating if an allocataire account exists.
 * Retourné par GestionnaireCompte indiquant si le compte existe.
 */
public record CheckAccountExistsResponse(
    boolean exists
) implements Message.Response {}

