package cy.cav.framework;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.*;

import java.util.*;

/// Identifies a unique [Actor] on the network or a server as a whole.
///
/// An [ActorAddress] is unique to each actor and cannot be reused.
///
/// The [#serverId()] must always be a valid server id.
///
/// The [#actorNumber()] can be 0, which means that this address refers to the server of id [#serverId()].
///
/// @param serverId    the identifier of the server in the network; is usually randomly generated
/// @param actorNumber the number of the actor in the server; is usually a sequence number that gets incremented each time the server creates a new Actor; zero refers to the server itself (like IP addresses)
public record ActorAddress(long serverId, long actorNumber) {
    private static final HexFormat format = HexFormat.of();

    /// Returns the id in hexadecimal format: `SSSSSSSSSSSSSSSS:NNNNNNNNNNNNNNNN`
    @Override
    @JsonValue
    public String toString() {
        return format.toHexDigits(serverId) + ':' + format.toHexDigits(actorNumber);
    }

    @JsonCreator
    public static @Nullable ActorAddress fromString(@Nullable String input) {
        if (input == null) {
            return null;
        }

        int sepIndex = input.indexOf(':');
        if (sepIndex == -1) {
            return null;
        }

        try {
            long serverId = HexFormat.fromHexDigitsToLong(input, 0, sepIndex);
            long actorNumber = HexFormat.fromHexDigitsToLong(input, sepIndex + 1, input.length());
            return new ActorAddress(serverId, actorNumber);
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    public static ActorAddress server(long serverId) {
        return new ActorAddress(serverId, 0);
    }

    public boolean isServerAddress() {
        return actorNumber == 0;
    }
}
