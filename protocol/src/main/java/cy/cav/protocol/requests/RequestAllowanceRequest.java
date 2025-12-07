package cy.cav.protocol.requests;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to create and process an allowance request.
 * The request is processed asynchronously ([allowance type] calculation, etc.), but the requestId
 * is returned immediately so the client can track the request.
 */
public record RequestAllowanceRequest(
    UUID beneficiaryId,
    String allowanceType,  // RSA, ARE, APL, PRIME_ACTIVITE
    double monthlyIncome,
    int numberOfDependents,
    boolean inCouple,
    boolean hasHousing
) implements Message.Request<RequestAllowanceResponse> {}

