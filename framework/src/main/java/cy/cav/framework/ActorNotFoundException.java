package cy.cav.framework;

/// Thrown during a request-response conversation using [Message.Response] when the actor wasn't found
/// while trying to deliver the message to it.
public class ActorNotFoundException extends Exception {
    public ActorNotFoundException(String message) {
        super(message);
    }
}
