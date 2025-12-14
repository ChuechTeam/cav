package cy.cav.protocol;

import java.math.*;

public record Payment(String label, BigDecimal amount) {
}
