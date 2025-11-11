package cy.cav.service;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;

@SpringBootApplication
public class CavApplication implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;

    public CavApplication(World world) {
        this.world = world;
    }

    static void main(String[] args) {
        SpringApplication.run(CavApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // Create a new greeter with a special id
        world.spawn(Greeter::new, KnownActors.GREETER);
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