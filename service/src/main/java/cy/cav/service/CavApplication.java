package cy.cav.service;

import cy.cav.framework.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

@SpringBootApplication
public class CavApplication implements ApplicationListener<ApplicationStartedEvent> {

    private final World world;

    public CavApplication(World world) { this.world = world; }

    public static void main(String[] args) {
        SpringApplication.run(CavApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ActorAddress addr = world.spawn(MyActor::new);
        world.query(null, addr, new Req("ahah")).thenAccept(System.out::println);
    }
}


class MyActor extends Actor {
    protected MyActor(ActorInit init) {
        super(init);
    }

    @Override
    protected void process(Envelope envelope) {
        if (envelope.body() instanceof Req(String test)) {
            respond(envelope, new Resp("test " + test));
        }
    }
}

record Req(String test) implements Message.WithResponse<Resp> {}
record Resp(String test) implements Message {}