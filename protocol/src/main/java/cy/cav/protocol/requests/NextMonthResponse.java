package cy.cav.protocol.requests;

import cy.cav.framework.*;

import java.time.*;

public record NextMonthResponse(LocalDate month) implements Message.Response {}
