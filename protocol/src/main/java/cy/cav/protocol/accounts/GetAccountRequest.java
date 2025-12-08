package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to get a beneficiary account's information.
 */
public record GetAccountRequest() implements Message.Request<GetAccountResponse> {}

