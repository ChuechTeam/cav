package cy.cav.protocol;

import cy.cav.framework.*;

public record HelloResponse(String message) implements Message.Response {
}
