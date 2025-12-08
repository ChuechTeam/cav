package cy.cav.protocol.accounts;

import cy.cav.framework.*;
import cy.cav.protocol.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response after creating a beneficiary account.
 */
public record CreateAccountResponse(
    UUID beneficiaryId,
    ActorAddress beneficiaryAddress
) implements Message.Response {}

