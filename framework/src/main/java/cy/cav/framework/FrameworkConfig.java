package cy.cav.framework;

import com.netflix.discovery.*;
import org.springframework.beans.factory.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.cloud.netflix.eureka.*;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.*;
import org.springframework.scheduling.annotation.*;

/// Configures all Spring Beans of the framework. Also configures Eureka.
@AutoConfiguration
@EnableScheduling
@PropertySource("classpath:application.properties")
@AutoConfigureBefore(EurekaClientAutoConfiguration.class)
public class FrameworkConfig {
    @Bean
    EurekaInit eurekaInit(EurekaInstanceConfigBean config, Server server) {
        return new EurekaInit(config, server);
    }

    @Bean
    World world(Server server, OutsideSender outsideSender) {
        return new World(server, outsideSender);
    }

    // Allows users of the framework to put their own Server settings.
    @Bean
    @ConditionalOnMissingBean
    Server server(Environment environment) {
        return new Server(environment);
    }

    @Bean
    OutsideSender outsideSender(ObjectProvider<EurekaClient> eurekaClientProvider, Server server) {
        return new OutsideSender(eurekaClientProvider, server);
    }

    @Bean
    OutsideReceiver outsideReceiver(World world, Server server) {
        return new OutsideReceiver(world, server);
    }

    /// Initializes the Eureka configuration with the right settings.
    private static class EurekaInit {
        EurekaInit(EurekaInstanceConfigBean config, Server server) {
            // Before we register this server with Eureka,
            // put the correct instance id: the hexadecimal format of this server's instance id.
            config.setInstanceId(server.idString());
        }
    }
}