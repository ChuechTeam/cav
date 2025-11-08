package cy.cav.framework;

import com.fasterxml.jackson.annotation.*;

/// A piece of data that can be the [message body][Envelope#body()] of an [Envelope].
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
public interface Message {
    /// A request that expects a response of type [T].
    ///
    /// Can be used in [World#query(cy.cav.framework.ActorAddress, cy.cav.framework.ActorAddress, cy.cav.framework.Message.WithResponse)].
    interface WithResponse<T extends Message> extends Message {}
}
