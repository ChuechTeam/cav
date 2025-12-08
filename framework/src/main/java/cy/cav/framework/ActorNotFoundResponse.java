package cy.cav.framework;

/// Sent as a response to a request when the actor wasn't found.
///
/// @param address the address of the actor which was not found
public record ActorNotFoundResponse(ActorAddress address) implements Message.Response {
}
