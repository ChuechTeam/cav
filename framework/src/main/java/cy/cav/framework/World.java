package cy.cav.framework;

import jakarta.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

@Component
public class World {
    private final BlockingQueue<Message> mailbox = new LinkedBlockingQueue<>();
    private final Map<ActorId, Actor> actors = new ConcurrentHashMap<>();
    private final long serverId;
    private final Random random;
    private volatile boolean continueRunning = true;

    private final AtomicLong nextActorNumber = new AtomicLong(1);

    public World() {
        this.random = new Random();
        this.serverId = this.random.nextLong(); // todo: use crypto random
    }

    // todo: launch on bean start
    @PostConstruct
    public void run() throws InterruptedException {
        while (continueRunning) {
            Message message = this.mailbox.take();

            ActorId dest = message.dest();
            Actor destActor = this.actors.getOrDefault(dest, null);

            if (destActor != null) {
                destActor.acceptMessage(message.body);
                // todo: what if this throws an exception?
            } else {
                // Then the actor is either:
                // - dead
                // - not a actor we know about
                //
                // So just ignore it!
            }
        }
    }

    @PreDestroy
    public void stop() {
        continueRunning = false;
    }

    public ActorId spawn(Function<ActorInit, Actor> creator) {
        long actorNumber = nextActorNumber.getAndIncrement();
        ActorId id = new ActorId(serverId, actorNumber);
        Actor actor = creator.apply(new ActorInit(this, id));

        actors.put(id, actor);

        actor.spawned(); // todo: what if this throws an exception?

        return id;
    }

    void despawn(ActorId id) {
        // Only the actor who's processing messages right now can despawn itself.
        Actor despawnedActor = actors.remove(id);
        if (despawnedActor != null) {
            despawnedActor.despawned(); // todo: what if this throws an exception?
        }
    }

    public void send(ActorId dest, Object body) {
        // todo: What happens if the server id is NOT this server?
        if (dest.serverId() != serverId) {
            throw new UnsupportedOperationException("Can't send messages to other servers yet!");
        }
        mailbox.add(new Message(dest, body)); // todo: use put if necessary
    }

    private record Message(ActorId dest, Object body) {}
}
