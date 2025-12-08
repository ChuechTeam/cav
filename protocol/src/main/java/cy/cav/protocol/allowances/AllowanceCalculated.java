package cy.cav.protocol.allowances;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;
import cy.cav.protocol.*;

import java.math.*;
import java.util.*;

// TODO: More info?
/// Result of a calculation request
///
/// @param message some additional info for the user concerning the calculation; empty string if there's none
public record AllowanceCalculated(
        AllowanceType type,
        BigDecimal amount,
        String message,
        UUID ackId
) implements Message.Notification, Acknowledgeable { }
