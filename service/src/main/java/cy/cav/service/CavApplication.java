package cy.cav.service;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.service.actors.*;
import cy.cav.service.config.*;
import org.slf4j.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;

@SpringBootApplication
public class CavApplication implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger log = LoggerFactory.getLogger(CavApplication.class);
    private final World world;
    private final Store store;
    private final DefaultBeneficiaries defaultBeneficiaries;
    private final Server server;
    private final ServerFinder serverFinder;

    public CavApplication(World world, Store store, DefaultBeneficiaries defaultBeneficiaries,
                          Server server, ServerFinder serverFinder) {
        this.world = world;
        this.store = store;
        this.defaultBeneficiaries = defaultBeneficiaries;
        this.server = server;
        this.serverFinder = serverFinder;
    }

    public static void main(String[] args) {
        SpringApplication.run(CavApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        boolean supportsPrefecture = Boolean.parseBoolean(server.metadata().getOrDefault("supportsPrefecture", ""));
        boolean supportsCalculators = Boolean.parseBoolean(server.metadata().getOrDefault("supportsCalculators", ""));

        if (!supportsPrefecture && !supportsCalculators) {
            throw new IllegalStateException("Can't have an app that doesn't support BOTH prefectures and calculators!");
        }

        log.info("Starting with supportsPrefecture={} ; supportsCalculators={}", supportsPrefecture, supportsCalculators);

        if (supportsPrefecture) {
            // Spawn prefecture actor (manages beneficiary actors)
            world.spawn(init -> new Prefecture(init, store, defaultBeneficiaries, serverFinder), KnownActors.PREFECTURE);
        }

        if (supportsCalculators) {
            // Spawn RSA calculator actor
            world.spawn(RSACalculator::new, KnownActors.RSA_CALCULATOR);
        }
    }
}