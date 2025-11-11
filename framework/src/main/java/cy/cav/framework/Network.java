package cy.cav.framework;

import com.netflix.appinfo.*;
import com.netflix.discovery.*;
import com.netflix.discovery.shared.*;
import jakarta.annotation.*;
import org.springframework.beans.factory.*;
import org.springframework.boot.web.context.*;
import org.springframework.context.event.EventListener;

import java.util.*;

public class Network {
    private final ObjectProvider<EurekaClient> eurekaClientProvider;
    private final List<String> applications;
    private final Server server;
    private @Nullable EurekaClient eurekaClient;

    private volatile Map<Long, Server> serverMap = Map.of();

    public Network(ObjectProvider<EurekaClient> eurekaClientProvider,
                   List<String> applications,
                   Server server) {
        this.eurekaClientProvider = Objects.requireNonNull(eurekaClientProvider);
        this.applications = applications.stream().map(String::toUpperCase).toList();
        this.server = Objects.requireNonNull(server);
    }

    @EventListener(WebServerInitializedEvent.class)
    private void onWebServerReady() {
        eurekaClient = eurekaClientProvider.getObject();
        eurekaClient.registerEventListener(this::onEurekaEvent);

        // Refresh the cache now!
        onEurekaEvent(new CacheRefreshedEvent());
    }

    @SuppressWarnings("unchecked")
    private void onEurekaEvent(EurekaEvent event) {
        if (!(event instanceof CacheRefreshedEvent) || eurekaClient == null) { return; }

        // The Eureka client has updated its cache, let's see which servers are available on the network and
        // update our local cache.

        var entries = new ArrayList<Map.Entry<Long, Server>>();

        // Look over all registered servers.
        for (Application application : eurekaClient.getApplications().getRegisteredApplications()) {
            // Make sure we care about that app
            if (!applications.contains(application.getName())) { continue; }

            for (InstanceInfo instance : application.getInstances()) {
                long serverId;
                try {
                    // Because the instance id is a string, we need to convert it to a number.
                    // Server ids are stored in hexadecimal format, so we need to indicate
                    // that it's in a hexa (16) format.
                    serverId = Long.parseUnsignedLong(instance.getInstanceId(), 16);
                } catch (NumberFormatException _) {
                    // Nothing to do, just not a valid id
                    return;
                }

                // Put the server in the map, and don't register ourselves, no need to.
                if (serverId != server.id()) {
                    Server newServer = new Server(serverId, application.getName(), instance.getHomePageUrl(),
                            instance.getMetadata());
                    entries.add(Map.entry(serverId, newServer));
                }
            }
        }

        serverMap = Map.ofEntries(entries.toArray(Map.Entry[]::new));
    }

    public Map<Long, Server> servers() {
        return serverMap;
    }
}
