package cy.cav.protocol;

import java.math.*;

public record AllowancePrevision(
        AllowanceType type,
        AllowancePrevisionState state,
        BigDecimal lastAmount,
        String lastMessage
) {
}
