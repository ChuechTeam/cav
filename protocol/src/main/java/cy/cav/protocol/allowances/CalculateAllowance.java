package cy.cav.protocol.allowances;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;
import cy.cav.protocol.*;

import java.util.*;

/**
 * Request to calculate any allowance.
 */
public record CalculateAllowance(
        BeneficiaryProfile profile,
        UUID ackId
) implements Message.Notification, Acknowledgeable {
    public CalculateAllowance(BeneficiaryProfile profile) {
        this(profile, UUID.randomUUID());
    }
}

