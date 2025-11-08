package cy.cav.framework;

import jakarta.inject.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.context.properties.*;
import org.springframework.core.env.*;
import org.springframework.stereotype.*;

import java.security.*;
import java.util.*;

/// Contains information about the server in the network: mainly, its unique id and the application name.
public class Server {
    private final Long id;
    private final ActorAddress address;
    private final String idString;
    private final String appName;

    Server(Environment environment) {
        try {
            // Pick a random server id using cryptographically secure randomness.
            // It is used for registration with the Eureka server.
            id = SecureRandom.getInstanceStrong().nextLong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SecureRandom isn't supported; cannot generate server id.", e);
        }

        // Hex-formatted string for good measure
        idString = HexFormat.of().toHexDigits(id);

        address = ActorAddress.server(id);

        // Store the application name from the environment
        appName = environment.getProperty("spring.application.name");
    }

    /// Returns the unique id of this server
    public Long id() {
        return id;
    }

    /// Returns the address identifying this server
    public ActorAddress address() {
        return address;
    }

    /// Returns the unique id of this server in hexadecimal string format.
    public String idString() {
        return idString;
    }

    /// Returns the name of the application this server runs; used for Eureka.
    public String appName() {
        return appName;
    }
}
