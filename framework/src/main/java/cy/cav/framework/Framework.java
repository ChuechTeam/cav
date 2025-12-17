package cy.cav.framework;

import com.netflix.discovery.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.*;
import org.springframework.cloud.netflix.eureka.*;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.*;
import org.springframework.scheduling.*;
import org.springframework.scheduling.annotation.*;

import java.security.*;
import java.util.*;

/// Configures all Spring Beans of the framework. Also configures Eureka.
@AutoConfiguration
@EnableScheduling
@PropertySource("classpath:application.properties")
@AutoConfigureBefore(EurekaClientAutoConfiguration.class)
@EnableConfigurationProperties(FrameworkConfig.class)
public class Framework {
    @Bean
    EurekaInit eurekaInit(EurekaInstanceConfigBean config, Server server) {
        return new EurekaInit(config, server);
    }

    @Bean
    World world(Server server, OutsideSender outsideSender, TaskScheduler taskScheduler) {
        return new World(server, outsideSender, taskScheduler);
    }

    // Allows users of the framework to put their own Server settings.
    @Bean
    @ConditionalOnMissingBean
    Server server(Environment environment, FrameworkConfig config, List<MetadataInit> metadataInits) {
        long id;
        if (config.serverId() != null) {
            id = HexFormat.fromHexDigitsToLong(config.serverId());
        } else {
            try {
                // Pick a random server id using cryptographically secure randomness.
                // It is used for registration with the Eureka server.
                id = SecureRandom.getInstanceStrong().nextLong();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SecureRandom isn't supported; cannot generate server id.", e);
            }
        }

        // Customize metadata according to MetadataInits
        var metadata = new HashMap<>(config.metadata());
        for (MetadataInit metadataInit : metadataInits) {
            metadataInit.populate(id, metadata);
        }

        // Finally, create the server
        return new Server(id, environment.getProperty("spring.application.name"), null, metadata);
    }

    @Bean
    @ConditionalOnMissingBean
    Network network(ObjectProvider<EurekaClient> eurekaClientProvider,
                    Server server,
                    FrameworkConfig config,
                    @Value("${spring.application.name}") String appName) {
        List<String> allApps;
        if (config.applications().isEmpty()) {
            allApps = List.of(appName);
        } else {
            allApps = config.applications();
        }

        return new Network(eurekaClientProvider, allApps, server);
    }

    @Bean
    OutsideSender outsideSender(Network network) {
        return new OutsideSender(network);
    }

    @Bean
    OutsideReceiver outsideReceiver(World world) {
        return new OutsideReceiver(world);
    }

    @Bean
    ActorAddress.Converter addressConverter() {
        return new ActorAddress.Converter();
    }

    /// Initializes the Eureka configuration with the right settings.
    private static class EurekaInit {
        EurekaInit(EurekaInstanceConfigBean config, Server server) {
            // Before we register this server with Eureka,
            // put the correct instance id: the hexadecimal format of this server's instance id.
            config.setInstanceId(server.idString());
            // And whatever metadata we happen to have.
            config.getMetadataMap().putAll(server.metadata());
        }
    }
}