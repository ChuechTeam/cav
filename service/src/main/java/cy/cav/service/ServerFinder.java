package cy.cav.service;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import jakarta.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

/// Finds a compatible server on the network when needing some specific actor.
@Component
public class ServerFinder {
    private final Network network;
    private final Server server;
    private final Random random;

    public ServerFinder(Network network, Server server) {
        this.network = network;
        this.server = server;
        this.random = new Random();
    }

    /// Finds a random server on the network that has calculator actors. When this server already supports calculators,
    /// returns this server.
    public @Nullable Server pickCalculatorServer() {
        // If this server already supports calculators, good!
        if (Boolean.parseBoolean(server.metadata().getOrDefault("supportsCalculators", ""))) {
            return server;
        }

        // Otherwise, pick one randomly off the network
        List<Server> calcServers = network.servers().values()
                .stream()
                .filter(s -> Boolean.parseBoolean(s.metadata().getOrDefault("supportsCalculators", "")))
                .toList();

        if (calcServers.isEmpty()) {
            return null;
        }

        return calcServers.get(random.nextInt(calcServers.size()));
    }

    /// Finds a random server on the network that has calculator actors
    /// and makes an actor address for the calculator of the allowance type.
    /// When this server already supports calculators, returns this server.
    public @Nullable ActorAddress pickCalculatorActor(AllowanceType type) {
        Server calcServ = pickCalculatorServer();
        if (calcServ == null) {
            return null;
        }

        return type.calculatorActor(calcServ);
    }
}
