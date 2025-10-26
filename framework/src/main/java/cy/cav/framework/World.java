package cy.cav.framework;

import jakarta.annotation.*;
import org.slf4j.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

import java.security.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

/// The world hosts all [actors][Actor] of the system and dispatches incoming messages to all actors.
///
/// ## What you can do with it
///
/// - spawn actors with [#spawn(java.util.function.Function)]
/// - send messages with [#send(ActorId, ActorId, Message)]
/// - start processing messages with [#start()]
///
/// ## What you CANNOT do with it
/// - despawn actors; only actors can despawn themselves using [Actor#despawn()]
///
/// @see Actor
@Component
public class World implements SmartLifecycle {
    // Used to write messages in the console with priorities (warning, info, error)
    private static final Logger log = LoggerFactory.getLogger(World.class);

    private final BlockingQueue<Envelope> mailbox = new LinkedBlockingQueue<>();
    private final Map<ActorId, Actor> actors = new ConcurrentHashMap<>();
    private final long serverId;

    private volatile boolean running = false;
    private volatile Thread mainLoopThread = null;

    private final AtomicLong nextActorNumber = new AtomicLong(1);

    /// Creates a new [World] with a random server id.
    public World() {
        try {
            // Pick a random server id using cryptographically secure randomness.
            // It is used for registration with the Eureka server.
            this.serverId = SecureRandom.getInstanceStrong().nextLong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SecureRandom isn't supported; cannot generate server id.", e);
        }
    }

    // todo: listen to eureka events and update local server id -> addr table

    /// Starts the message-processing loop in a new thread running in the background.
    ///
    /// The thread will continue to process incoming messages until the world is stopped using [#stop()].
    public void start() {
        if (mainLoopThread != null) {
            throw new IllegalStateException("The world cannot start more than one time.");
        }
        running = true;
        mainLoopThread = Thread.ofPlatform()
                .name("World Main Loop")
                .start(this::startMainLoop);
    }

    /// Stops the message-processing loop. Any message left in the queue will be dropped.
    ///
    /// Does nothing if the loop isn't running.
    public void stop() {
        running = false; // Stops the "while" loop
        mainLoopThread.interrupt(); // Stops the "mailbox.take()" call
    }

    /// Returns true if the world is active and dispatching messages to actors.
    public boolean isRunning() {
        return running;
    }

    private void startMainLoop() {
        log.info("Starting World main loop with serverId {}", Long.toHexString(serverId));

        while (running) {
            // First off, read the incoming envelope from the queue.
            // If there's no incoming envelope yet, the "take()" call will wait until one arrives.
            Envelope envelope;
            try {
                envelope = mailbox.take();
            } catch (InterruptedException e) {
                // The thread is stopping; stop listening to messages in the envelope queue.
                return;
            }

            // Find the actor to send the envelope to.
            ActorId receiver = envelope.receiver();
            Actor receiverActor = actors.getOrDefault(receiver, null);

            if (receiverActor != null) {
                // The actor has been found! Let it process the envelope.
                log.info("Received envelope {} for actor {}", envelope, receiverActor);
                receiverActor.acceptEnvelope(envelope);
                // todo: what if this throws an exception?
            } else {
                // Then the actor is either:
                // - dead
                // - not a actor we know about
                //
                // So just ignore it!
                log.warn("Received envelope {} for an unknown actor!", envelope);
            }
        }
    }

    /// Spawns a new actor using the given creator function.
    ///
    /// The creator function accepts an [ActorInit] object, containing the id of the new actor (among other things),
    /// and returns an [Actor].
    ///
    /// ## Example
    ///
    /// ```java
    /// ActorId id = world.spawn(init -> new MyActor(init));
    /// ActorId id = world.spawn(MyActor::new); // Same thing
    /// ActorId otherId = world.spawn(init -> new OtherActor(init, 1234));
    ///
    /// class MyActor extends Actor {
    ///     MyActor(ActorInit init) { super(init); }
    /// }
    ///
    /// class OtherActor extends Actor {
    ///     int x;
    ///     OtherActor(ActorInit init, int x) { super(init); this.x = x; }
    /// }
    /// ```
    ///
    /// @param creator the creator function
    /// @return the id of the newly created actor
    public ActorId spawn(Function<ActorInit, Actor> creator) {
        // Pick the next actor number by incrementing the global counter,
        // and use the serverId to make the final ActorId.
        long actorNumber = nextActorNumber.getAndIncrement();
        ActorId id = new ActorId(serverId, actorNumber);

        // Create the actor using the function the user gave us. Give it the id we made up.
        Actor actor = creator.apply(new ActorInit(this, id));

        Objects.requireNonNull(actor, "The created actor is null!");

        // Register the actor in the map of existing actors and let it know that we've spawned it.
        actors.put(id, actor);
        actor.reportSpawned(); // todo: what if this throws an exception? + possible race condition

        log.info("New actor of id {} spawned: {}", id, actor);

        // Return the id we created.
        return id;
    }

    /// Called only by [Actor] to despawn itself. Always use [Actor#despawn()].
    /// This thing is package-protected for a reason...
    void despawn(ActorId id) {
        // Remove it from the map. If it really exists, make sure it's aware of its death.
        Actor despawnedActor = actors.remove(id);
        if (despawnedActor != null) {
            despawnedActor.reportDespawned(); // todo: what if this throws an exception?
            log.info("Actor {} despawned: {}", id, despawnedActor);
        }
    }

    /// Sends a message to the actor with id `dest`, with the given body and sender actor.
    ///
    /// Supports sending messages to other servers. (Well not yet but it should.)
    ///
    /// Messages are NOT guaranteed to be sent to the destination actor.
    ///
    /// @param sender    the actor that sent the message; can be null
    /// @param recipient the id of the actor to send the message to
    /// @param body      the body of the message
    public void send(@Nullable ActorId sender, ActorId recipient, Message body) {
        Objects.requireNonNull(body, "Body shouldn't be null!");
        Objects.requireNonNull(recipient, "Recipient shouldn't be null!");

        // Put the message in an envelope, so the postman "knows" which actor to send the message to.
        var envelope = new Envelope(sender, recipient, body, Instant.now());

        if (recipient.serverId() == serverId) {
            // The actor we want to send the message to is in this world!
            // Just add the envelope to our local queue.
            mailbox.add(envelope);
            log.info("Sent envelope {} to this world's mailbox", envelope);
        } else {
            // todo: Be able to send messages to other servers
            throw new UnsupportedOperationException("Can't send messages to other servers yet!");
        }
    }

    /// Returns the server id of this world.
    public long serverId() {
        return serverId;
    }
}
