package cy.cav.protocol.requests;

import cy.cav.framework.*;
import java.time.LocalDate;

public record PrefectureStateResponse(String status, LocalDate currentMonth) implements Message.Response {
}
