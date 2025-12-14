package cy.cav.protocol.accounts;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import jakarta.annotation.*;

import java.util.*;

/**
 * Response with beneficiary account information.
 */
public record GetAccountResponse(
        UUID beneficiaryId,
        BeneficiaryProfile profile,
        List<Payment> payments,
        Map<AllowanceType, AllowancePrevision> allowancePrevisions
) implements Message.Response {
}

