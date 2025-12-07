package cy.cav.service;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.service.actors.Prefecture;
import cy.cav.service.actors.AllowanceRequest;
import cy.cav.service.actors.calculateurs.RSACalculator;
import cy.cav.service.store.AllocationStore;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;

@SpringBootApplication
public class CavApplication implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;
    private final AllocationStore store;

    public CavApplication(World world, AllocationStore store) {
        this.world = world;
        this.store = store;
    }

    public static void main(String[] args) {
        SpringApplication.run(CavApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // Create a new greeter with a special id
        world.spawn(Greeter::new, KnownActors.GREETER);
        
        // Spawn prefecture actor (manages beneficiary actors)
        world.spawn(init -> new Prefecture(init, store), KnownActors.PREFECTURE);
        
        // Spawn allowance request actor (manages allowance requests)
        world.spawn(init -> new AllowanceRequest(init, store), KnownActors.ALLOWANCE_REQUEST);
        
        // Spawn RSA calculator actor
        world.spawn(init -> new RSACalculator(init, store), KnownActors.RSA_CALCULATOR);
    }
}

class Greeter extends Actor {
    public Greeter(ActorInit init) {
        super(init);
    }

    static final Router<Greeter> router = new Router<Greeter>()
            .route(HelloRequest.class, Greeter::greet);

    @Override
    protected void process(Envelope<?> envelope) { router.process(this, envelope); }

    HelloResponse greet(HelloRequest request) {
        return new HelloResponse("Hello " + request.name());
    }
}