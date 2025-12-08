package cy.cav.protocol.requests;

import cy.cav.framework.Message;
import cy.cav.protocol.*;

import java.util.UUID;

/**
 * Request to create and process an allowance request.
 * The request is processed asynchronously ([allowance type] calculation, etc.).
 */
public record RequestAllowanceRequest(
    AllowanceType type
) implements Message.Request<RequestAllowanceResponse> {}

