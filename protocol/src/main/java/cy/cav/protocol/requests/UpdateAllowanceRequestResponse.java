package cy.cav.protocol.requests;

import cy.cav.framework.Message;

/**
 * Response after updating an allowance request.
 */
public record UpdateAllowanceRequestResponse(
    boolean success
) implements Message.Response {}

