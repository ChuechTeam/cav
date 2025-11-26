package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to check if a beneficiary account exists.
 */
public record CheckAccountExistsRequest(
    UUID beneficiaryId
) implements Message.Request<CheckAccountExistsResponse> {}

