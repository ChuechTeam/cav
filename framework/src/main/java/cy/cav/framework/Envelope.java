package cy.cav.framework;

import jakarta.annotation.*;

import java.time.*;

/// Holds a [Message] with additional data: the sender, the receiver, and time info.
///
/// This is what gets sent on the wire in HTTP REST communications.
///
/// @param sender   the actor who sent the message; can be null
/// @param receiver the actor who should receive the message
/// @param body     the data of the message
/// @param sentAt   the time at which the message has been sent
public record Envelope(
        @Nullable ActorId sender,
        ActorId receiver,
        Message body,
        Instant sentAt
) {}
