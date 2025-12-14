package cy.cav.protocol.allowances;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;
import cy.cav.protocol.*;

import java.math.*;
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

    /// Result of a calculation request
    ///
    /// @param message some additional info for the user concerning the calculation; empty string if there's none
    public record Ack(
            AllowanceType type,
            BigDecimal amount,
            String message,
            UUID ackId
    ) implements Notification, Acknowledgeable { }
}

