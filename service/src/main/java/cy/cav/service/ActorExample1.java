package cy.cav.service;

import cy.cav.framework.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

@Component
@ConditionalOnBooleanProperty("cav.example1")
class ActorExample1 implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;

    ActorExample1(World world) { this.world = world; }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
    }
}

class Cook extends Actor {
    protected Cook(ActorInit init) {
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
        if (envelope.sender().equals(ELON_MUSK_ADDRESS)) {
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

record SandwichRequest(boolean vegetarian) implements Message.WithResponse<SandwichResponse> { }

record SandwichResponse(String ingredients) implements Message { }

record PizzaRequest() implements Message.WithResponse<PizzaResponse> { }

record PizzaResponse(String ingredients) implements Message { }

record YouGotPaidMessage(int amount) implements Message { }

record YouAreFiredMessage() implements Message { }

