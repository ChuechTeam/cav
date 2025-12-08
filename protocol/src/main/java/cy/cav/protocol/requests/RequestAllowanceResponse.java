package cy.cav.protocol.requests;

import cy.cav.framework.Message;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response after creating an allowance request.
 * Contains the requestId immediately, allowing the client to track the request.
 */
public record RequestAllowanceResponse(
    boolean success,
    String message
) implements Message.Response {}

