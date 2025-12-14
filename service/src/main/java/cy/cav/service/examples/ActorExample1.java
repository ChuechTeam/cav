package cy.cav.service.examples;

import cy.cav.framework.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

import java.util.*;

@Component
@ConditionalOnBooleanProperty("cav.example1")
class ActorExample1 implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;

    ActorExample1(World world) { this.world = world; }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ActorAddress cookAddress = world.spawn(Cook::new);

        world.send(null, cookAddress, new YouGotPaidMessage(150));

        world.query(null, cookAddress, new SandwichRequest(true))
                .thenAccept(response -> System.out.println("Sandwich reçu : " + response.ingredients()));

        world.query(null, cookAddress, new PizzaRequest())
                .thenAccept(response -> System.out.println("Pizza reçue : " + response.ingredients()));

        ActorAddress hrAddress = world.spawn(ToxicHR::new);

        world.send(null, hrAddress, new FireSomeone(cookAddress));
    }
}

class Cook extends Actor {
    Cook(ActorInit init) {
        super(init);
    }

    static final Router<Cook> router = new Router<Cook>()
            .route(SandwichRequest.class, Cook::giveSandwich)
            .route(PizzaRequest.class, Cook::givePizza)
            .route(YouGotPaidMessage.class, Cook::paid)
            .route(YouAreFiredMessage.class, Cook::fired);

    @Override
    protected void process(Envelope<?> envelope) { router.process(this, envelope); }

    SandwichResponse giveSandwich(SandwichRequest request) {
        if (request.vegetarian()) {
            return new SandwichResponse("Sandwich à la betterave");
        } else {
            return new SandwichResponse("Sandwich au thon");
        }
    }

    PizzaResponse givePizza(Envelope<PizzaRequest> envelope) {
        if (Objects.equals(envelope.sender(), ELON_MUSK_ADDRESS)) {
            return new PizzaResponse("Pizza Tesla");
        } else {
            return new PizzaResponse("Pizza quatre fromages");
        }
    }

    void paid(YouGotPaidMessage message) {
        System.out.println("J'ai été payé " + message.amount() + "€ !");
    }

    void fired(Envelope<YouAreFiredMessage> envelope) {
        System.out.println("J'ai été viré par " + envelope.sender());
    }

    static final ActorAddress ELON_MUSK_ADDRESS = new ActorAddress(0, 10);
}

class ToxicHR extends Actor {
    ToxicHR(ActorInit init) {
        super(init);
    }

    static final Router<ToxicHR> router = new Router<ToxicHR>()
            .route(FireSomeone.class, ToxicHR::fire);

    @Override
    protected void process(Envelope<?> envelope) { router.process(this, envelope); }

    void fire(FireSomeone fireSomeone) {
        System.out.println("Je vais virer un employé j'adore ça");

        send(fireSomeone.actor(), new YouAreFiredMessage());
    }
}

record SandwichRequest(boolean vegetarian) implements Message.Request<SandwichResponse> { }

record SandwichResponse(String ingredients) implements Message.Response { }

record PizzaRequest() implements Message.Request<PizzaResponse> { }

record PizzaResponse(String ingredients) implements Message.Response { }

record YouGotPaidMessage(int amount) implements Message.Notification { }

record YouAreFiredMessage() implements Message.Notification { }

record FireSomeone(ActorAddress actor) implements Message.Notification { }