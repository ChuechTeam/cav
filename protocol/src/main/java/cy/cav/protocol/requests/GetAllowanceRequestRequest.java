package cy.cav.protocol.requests;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to get an allowance request by ID.
 */
public record GetAllowanceRequestRequest(
    UUID requestId
) implements Message.Request<GetAllowanceRequestResponse> {}

