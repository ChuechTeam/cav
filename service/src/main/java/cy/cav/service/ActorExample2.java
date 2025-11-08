package cy.cav.service;

import cy.cav.framework.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

@Component
@ConditionalOnBooleanProperty("cav.example2")
class ActorExample2 implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;

    ActorExample2(World world) { this.world = world; }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ActorAddress cookerAddr = world.spawn(Cooker::new);
        ActorAddress customerAddr = world.spawn(init -> new Customer(init, cookerAddr));
    }
}

class Cooker extends Actor {
    protected Cooker(ActorInit init) {
        super(init);
    }

    static final Router<Cooker> router = new Router<Cooker>()
            .route(MakeMeASandwich.class, Cooker::handle);

    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }

    SandwichPrepared handle(MakeMeASandwich request) {
        System.out.println("Received the request: " + request);

        double price = request.ingredient().equals("tomate") ? 80 : 40;

        return new SandwichPrepared(price);
    }
}

class Customer extends Actor {
    private final ActorAddress cookerAddr;

    protected Customer(ActorInit init, ActorAddress cookerAddr) {
        super(init);
        this.cookerAddr = cookerAddr;
    }

    @Override
    protected void spawned() {
        query(cookerAddr, new MakeMeASandwich("fromage"))
                .thenAccept(response -> {
                    System.out.println("I received the sandwich! Cost: " + response.price());
                });

        System.out.println("I'm the customer. " + address);
    }

    @Override
    protected void process(Envelope<?> envelope) {
        if (envelope.body() instanceof SayHi) {
            System.out.println("Hi from customer");
        }
    }
}

record MakeMeASandwich(String ingredient) implements Message.WithResponse<SandwichPrepared> {
}

record SandwichPrepared(double price) implements Message {
}

record SayHi() implements Message { }
