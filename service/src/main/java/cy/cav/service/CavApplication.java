package cy.cav.service;

import cy.cav.framework.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class CavApplication implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;

    public CavApplication(World world) {
        this.world = world;
    }

    public static void main(String[] args) {
        SpringApplication.run(CavApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ActorAddress cookerAddr = world.spawn(Cooker::new);
        ActorAddress customerAddr = world.spawn(init -> new Customer(init, cookerAddr));
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

class Cooker extends Actor {
    protected Cooker(ActorInit init) {
        super(init);
    }

    @Override
    protected void process(Envelope<?> envelope) {
        Message body = envelope.body();

        if (body instanceof MakeMeASandwich request) {
            System.out.println("Received the request: " + request);

            double price = request.ingredient().equals("tomate") ? 80 : 40;

            respond(envelope, new SandwichPrepared(price));
        }
    }
}

record MakeMeASandwich(String ingredient) implements Message.WithResponse<SandwichPrepared> {
}

record SandwichPrepared(double price) implements Message {
}

record SayHi() implements Message {}

@RestController
class Ctrl {
    private final World world;

    Ctrl(World world) { this.world = world; }

    @GetMapping("/send/{to}")
    ResponseEntity<?> send(@PathVariable String to) {
        world.send(null, ActorAddress.fromString(to), new SayHi());
        return ResponseEntity.ok().build();
    }
}