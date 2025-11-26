package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

/**
 * Response indicating if a beneficiary account exists.
 */
public record CheckAccountExistsResponse(
    boolean exists
) implements Message.Response {}

