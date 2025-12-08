package cy.cav.service;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.service.actors.Prefecture;
import cy.cav.service.actors.calculateurs.RSACalculator;
import cy.cav.service.config.*;
import cy.cav.service.store.AllocationStore;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;

@SpringBootApplication
public class CavApplication implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;
    private final AllocationStore store;
    private final DefaultBeneficiaries defaultBeneficiaries;

    public CavApplication(World world, AllocationStore store, DefaultBeneficiaries defaultBeneficiaries) {
        this.world = world;
        this.store = store;
        this.defaultBeneficiaries = defaultBeneficiaries;
    }

    public static void main(String[] args) {
        SpringApplication.run(CavApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // Spawn prefecture actor (manages beneficiary actors)
        world.spawn(init -> new Prefecture(init, store, defaultBeneficiaries), KnownActors.PREFECTURE);

        // Spawn RSA calculator actor
        world.spawn(RSACalculator::new, KnownActors.RSA_CALCULATOR);
    }
}