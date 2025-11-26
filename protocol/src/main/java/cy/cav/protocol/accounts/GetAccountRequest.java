package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to get a beneficiary account by ID.
 */
public record GetAccountRequest(
    UUID beneficiaryId
) implements Message.Request<GetAccountResponse> {}

