package cy.cav.service;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.util.*;

@Component
@ConditionalOnBooleanProperty("cav.example3")
class ActorExample3 implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;

    ActorExample3(World world) { this.world = world; }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ActorAddress centerAddress = world.spawn(Calculator::new);
        world.spawn(i -> new Client(i, centerAddress));
    }
}

class Client extends Actor {
    private final ActorAddress calculator;

    Client(ActorInit init, ActorAddress calculator) {
        super(init);
        this.calculator = calculator;
    }

    static final Random rng = new Random();

    static final Router<Client> router = new Router<Client>()
            .route(CalculationDone.class, Client::calculateDone);

    // Retries every two seconds.
    final AckRetryer retryer = AckRetryer.constantDelay(this, Duration.ofSeconds(2));

    @Override
    protected void spawned() {
        log.info("--- FIRST REQUEST STARTING SOON ---");
        retryer.sendDelayed(calculator, new Calculate(25, UUID.randomUUID()), Duration.ofSeconds(1));
    }

    void calculateDone(CalculationDone response) {
        log.info("CLIENT: Calculation received with {} euros!", response.euros());
        log.info("--- NEW REQUEST IN 5 SECONDS ---");
        retryer.sendDelayed(calculator, new Calculate(25, UUID.randomUUID()), Duration.ofSeconds(5));
    }

    @Override
    protected void process(Envelope<?> envelope) {
        // Simulate network issues, 50% of not receiving message.
        if (envelope.body() instanceof CalculationDone && rng.nextDouble() < 0.5) {
            log.info("CLIENT: Woops! I didn't hear your response! Please send it back!!");
            return;
        }

        // Remember to put this so the retryer can understand that we received the response to a request!
        // note: I should probably make it so you don't have to do this manually, but one step at a time!
        if (retryer.process(envelope)) {
            return;
        }

        // Let the router do its work
        router.process(this, envelope);
    }
}

class Calculator extends Actor {
    Calculator(ActorInit init) {
        super(init);
    }

    static final Router<Calculator> router = new Router<Calculator>()
            .route(Calculate.class, Calculator::calculate);

    static final Random rng = new Random();

    final AckStore<CalculationDone> calculationStore = new AckStore<>(this);

    @Override
    protected void process(Envelope<?> envelope) { router.process(this, envelope); }

    void calculate(Envelope<Calculate> calcEnvelope) {
        if (calculationStore.sendIfAcknowledged(calcEnvelope)) {
            log.info("CALCULATOR: I already sent this calculation!");
            return;
        }

        // oops! i fail 40% of the time!
        if (rng.nextDouble() < 0.4) {
            log.warn("CALCULATOR: Calculation failed!");
            return;
        }

        Calculate body = calcEnvelope.body();
        CalculationDone response = new CalculationDone(body.age()*rng.nextInt(20, 80), body.ackId());
        log.info("CALCULATOR: Calculation successful!");
        calculationStore.send(calcEnvelope.sender(), response);
    }
}

// The "ackId" is a unique id that is the same through a request/response conversation.
// For example, when i send Calculate with 123 as the ackId, I will receive CalculationDone with 123 as the ackId.
record Calculate(int age, UUID ackId) implements Message.Notification, Acknowledgeable { }

record CalculationDone(int euros, UUID ackId) implements Message.Notification, Acknowledgeable { }