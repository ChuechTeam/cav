package cy.cav.protocol;

import cy.cav.framework.*;

public record HelloRequest(String name) implements Message.Request<HelloResponse> {
}
