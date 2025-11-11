package cy.cav.framework;

import jakarta.annotation.*;

import java.util.*;

/// Contains information about the server in the network: mainly, its unique id and the application name.
///
/// @param id       the unique id of this server
/// @param address  the address identifying this server
/// @param idString the unique id of this server in hexadecimal string format
/// @param appName  the name of the application this server runs; used for Eureka.
/// @param url      the url of this server; can be null when the server is the one running the app
/// @param metadata the metadata registered for this server in Eureka
public record Server(Long id, ActorAddress address, String idString, String appName, @Nullable String url,
                     Map<String, String> metadata) {
    public Server(Long id, String appName, @Nullable String url, Map<String, String> metadata) {
        this(id, ActorAddress.server(id), HexFormat.of().toHexDigits(id), appName, url, Map.copyOf(metadata));
    }

    /// Makes an actor address with this server's id and the given actor number.
    public ActorAddress address(long actorNumber) {
        return new ActorAddress(id, actorNumber);
    }
}
