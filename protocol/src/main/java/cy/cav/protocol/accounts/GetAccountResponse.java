package cy.cav.protocol.accounts;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cy.cav.framework.Message;
import cy.cav.protocol.AllowancePrevision;
import cy.cav.protocol.AllowanceType;
import cy.cav.protocol.BeneficiaryProfile;
import cy.cav.protocol.Payment;

/**
 * Response with beneficiary account information.
 */
public record GetAccountResponse(
        UUID beneficiaryId,
        BeneficiaryProfile profile,
        List<Payment> payments,
        Map<AllowanceType, AllowancePrevision> allowancePrevisions,
        LocalDate currentMonth
) implements Message.Response {
}

