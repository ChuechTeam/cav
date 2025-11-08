package cy.cav.framework;

import com.fasterxml.jackson.annotation.*;

/// A piece of data that can be the [message body][Envelope#body()] of an [Envelope].
///
/// Messages come in three variants:
/// - **Asynchronous**
///   - notifications: [Message.Notification]
/// - **Synchronous**
///   - requests: [Message.Request]
///   - responses: [Message.Response]
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
public sealed interface Message permits Message.Notification, Message.Request, Message.Response {
    /// A **notification** can be sent to an actor **without expecting anything in return**.
    ///
    /// ## How to send a notification
    /// - from an actor: [Actor#send(ActorAddress, Message.Notification)]
    /// - from the world: [World#send(ActorAddress, ActorAddress, Message.Notification)]
    non-sealed interface Notification extends Message { }

    /// A **request** can be sent to an actor, **expecting a response** of type [T] in a **short amount of time**.
    ///
    /// Once a request is complete, the **sender is directly notified** and can take action (using a [java.util.concurrent.CompletionStage]).
    ///
    /// Requests can also be sent in a "fire-and-forget" version like a [Notification]. When this happens, the
    /// receiver doesn't send any message.
    ///
    /// ## How to send a request
    ///
    /// - **Waiting for the response**
    ///   - from an actor: [Actor#query(cy.cav.framework.ActorAddress, cy.cav.framework.Message.Request)]
    ///   - from the world: [World#query(cy.cav.framework.ActorAddress, cy.cav.framework.ActorAddress, cy.cav.framework.Message.Request)]
    /// - **Without waiting for the response**
    ///   - from an actor: [Actor#send(cy.cav.framework.ActorAddress, cy.cav.framework.Message.Notification)]
    ///   - from the world: [World#send(cy.cav.framework.ActorAddress, cy.cav.framework.ActorAddress, cy.cav.framework.Message.Notification)]
    non-sealed interface Request<T extends Response> extends Message { }

    /// A response to a request. Can only be sent in the context of a request.
    ///
    /// ## How to send a response
    ///
    /// From an actor: [Actor#respond(cy.cav.framework.Envelope, cy.cav.framework.Message.Response)]
    ///
    /// @see Actor#respond(Envelope, Response)
    non-sealed interface Response extends Message { }
}
