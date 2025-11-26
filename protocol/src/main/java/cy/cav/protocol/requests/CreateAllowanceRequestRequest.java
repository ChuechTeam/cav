package cy.cav.protocol.requests;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to create a new allowance request.
 * Sent from client to AllowanceRequest actor (service).
 */
public record CreateAllowanceRequestRequest(
    UUID beneficiaryId,
    String allowanceType  // RSA, ARE, APL, PRIME_ACTIVITE
) implements Message.Request<CreateAllowanceRequestResponse> {}

