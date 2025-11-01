package cy.cav.framework;

import com.fasterxml.jackson.annotation.*;

/// A piece of data that can be the [message body][Envelope#body()] of an [Envelope].
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
public interface Message {
    interface WithResponse<T extends Message> extends Message {}
}
