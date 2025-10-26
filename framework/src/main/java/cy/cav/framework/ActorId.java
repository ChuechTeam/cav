package cy.cav.framework;

import java.util.*;

/// Identifies a unique [Actor] on the network.
///
/// An [ActorId] is unique to each actor and cannot be reused.
///
/// @param serverId    the identifier of the server in the network; is usually randomly generated
/// @param actorNumber the number of the actor in the server; is usually a sequence number that gets incremented
///                                                                                                each time the server creates a new Actor
public record ActorId(long serverId, long actorNumber) {
    private static final HexFormat format = HexFormat.of();

    /// Returns the id in hexadecimal format: `SSSSSSSSSSSSSSSS:NNNNNNNNNNNNNNNN`
    @Override
    public String toString() {
        return format.toHexDigits(serverId) + ':' + format.toHexDigits(actorNumber);
    }
}
