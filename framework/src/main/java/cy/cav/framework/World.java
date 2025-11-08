package cy.cav.framework;

import jakarta.annotation.*;
import org.slf4j.*;
import org.springframework.context.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

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
/// - send messages with [#send(ActorAddress, ActorAddress, Message)]
/// - send requests with [#query(ActorAddress, ActorAddress, cy.cav.framework.Message.WithResponse)]
/// - start processing messages with [#start()]
///
/// ## What you CANNOT do with it
/// - despawn actors; only actors can despawn themselves using [Actor#despawn()]
///
/// @see Actor
public class World implements SmartLifecycle {
    // Used to write messages in the console with priorities (warning, info, error)
    private static final Logger log = LoggerFactory.getLogger(World.class);

    private final BlockingQueue<Envelope<?>> mailbox = new LinkedBlockingQueue<>();
    private final ConcurrentMap<Long, Actor> actors = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    private final Server server;
    private final OutsideSender outsideSender;

    private volatile boolean running = false;
    private volatile Thread mainLoopThread = null;

    private final AtomicLong nextActorNumber = new AtomicLong(1);
    private final AtomicLong nextRequestId = new AtomicLong(1);

    /// Creates a new [World]. Called by [FrameworkConfig].
    World(Server server, OutsideSender outsideSender) {
        this.server = Objects.requireNonNull(server);
        this.outsideSender = Objects.requireNonNull(outsideSender);
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

    @SuppressWarnings("unchecked")
    private void startMainLoop() {
        log.info("Starting World main loop with serverId {}", server.idString());

        while (running) {
            // First off, read the incoming envelope from the queue.
            // If there's no incoming envelope yet, the "take()" call will wait until one arrives.
            Envelope<?> envelope;
            try {
                envelope = mailbox.take();
            } catch (InterruptedException e) {
                // The thread is stopping; stop listening to messages in the envelope queue.
                return;
            }

            // Make sure this envelope is actually destined to this server. Else it doesn't make any sense.
            if (envelope.receiver().serverId() != server.id()) {
                log.warn("Somehow an envelope with the wrong server id made its way into this world's mailbox: {}", envelope);
                continue;
            }

            // See if this envelope is destined to this server, in which case we're processing a
            // response to a request.
            if (envelope.receiver().isServerAddress()) {
                // Find the request this envelope responds to.
                PendingRequest request = pendingRequests.remove(envelope.requestId());
                if (request == null) {
                    log.warn("Unknown request {} for response given by envelope: {}", envelope.requestId(), envelope);
                    continue;
                }

                // Complete the future with the message contained inside the envelope.
                log.info("Received response envelope for request {}: {}", envelope.requestId(), envelope);
                try {
                    ((CompletableFuture<Object>) request.future).complete(envelope.body());
                } catch (Exception e) {
                    log.error("Exception occurred while processing request response for envelope {}", envelope, e);
                }

                // We're done; read the next envelope.
                continue;
            }

            // Find the actor to send the envelope to.
            Actor receiver = actors.getOrDefault(envelope.receiver().actorNumber(), null);
            if (receiver != null) {
                // The actor has been found! Let it process the envelope.
                log.info("Received envelope for actor {}: {}", receiver, envelope);
                try {
                    receiver.acceptEnvelope(envelope);
                } catch (Exception e) {
                    // todo: supervision
                    log.error("Exception occured while actor {} is processing envelope {}", receiver, envelope, e);
                }
            } else {
                // Then the actor is either:
                // - dead
                // - not a actor we know about
                //
                // So just ignore it!
                log.warn("Received envelope for an unknown actor: {}", envelope);
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
    public ActorAddress spawn(Function<ActorInit, Actor> creator) {
        // Pick the next actor number by incrementing the global counter,
        // and use the serverId to make the final ActorId.
        long actorNumber = nextActorNumber.getAndIncrement();
        ActorAddress id = new ActorAddress(server.id(), actorNumber);

        // Create the actor using the function the user gave us. Give it the id we made up.
        Actor actor = creator.apply(new ActorInit(this, id));

        Objects.requireNonNull(actor, "The created actor is null!");

        // Register the actor in the map of existing actors and let it know that we've spawned it.
        actors.put(id.actorNumber(), actor);
        actor.reportSpawned(); // todo: what if this throws an exception? + possible race condition

        log.info("New actor of id {} spawned: {}", id, actor);

        // Return the id we created.
        return id;
    }

    /// Called only by [Actor] to despawn itself. Always use [Actor#despawn()].
    /// This thing is package-protected for a reason...
    void despawn(Long actorNumber) {
        // Remove it from the map. If it really exists, make sure it's aware of its death.
        Actor despawnedActor = actors.remove(actorNumber);
        if (despawnedActor != null) {
            despawnedActor.reportDespawned(); // todo: what if this throws an exception?
            log.info("Actor number {} despawned: {}", despawnedActor.address, despawnedActor);
        }
    }

    /// Sends a message to an actor, using the given body and sender actor.
    ///
    /// Supports sending messages to other servers.
    ///
    /// Messages are NOT guaranteed to be sent to the destination actor.
    ///
    /// @param sender   the actor that sent the message; can be null
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    public void send(@Nullable ActorAddress sender, ActorAddress receiver, Message body) {
        // Put the message in an envelope, so the postman "knows" which actor to send the message to.
        var envelope = new Envelope<>(sender, receiver, 0, body, Instant.now());
        sendEnvelope(envelope);
    }

    /// Begins a request-response conversation to an actor, by sending the given body and sender actor.
    ///
    /// Needs a message of type [Message.WithResponse] to know what the response will be.
    ///
    /// Requests aren't guaranteed to be sent to the destination actor.
    ///
    /// @param sender   the actor that sent the message; can be null
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    ///
    /// @return a [CompletionStage] which will complete successfully once the actor responds properly, or with a failure
    ///         when the actor fails to respond after a certain amount of time
    public <T extends Message> CompletionStage<T> query(@Nullable ActorAddress sender,
                                                        ActorAddress receiver,
                                                        Message.WithResponse<T> body) {
        // Create a future for this request, which will complete once we receive the response.
        // TODO: Configurable timeout
        var requestId = nextRequestId.getAndIncrement();
        var future = new CompletableFuture<T>();
        pendingRequests.put(requestId, new PendingRequest(future, Instant.now().plusSeconds(30)));

        // Put the message in an envelope, so the postman "knows" which actor to send the message to.
        var envelope = new Envelope<>(sender, receiver, requestId, body, Instant.now());
        sendEnvelope(envelope);

        // Return the future we've created earlier.
        return future;
    }

    /// Can only be called by [Actor]; it doesn't make sense to respond to requests outside an actor.
    void respond(@Nullable ActorAddress sender, Envelope<?> envelope, Message body) {
        if (envelope.requestId() == 0) {
            throw new IllegalArgumentException("Can't respond to an envelope with no request id!");
        }

        // Put the message in an envelope, so the postman "knows" which actor to send the message to.
        var newEnv = new Envelope<>(sender, server.address(), envelope.requestId(), body, Instant.now());
        sendEnvelope(newEnv);
    }

    private void sendEnvelope(Envelope<?> envelope) {
        if (envelope.receiver().serverId() == server.id()) {
            // The actor we want to send the message to is in this world!
            // Just add the envelope to our local queue.
            mailbox.add(envelope);
            log.info("Sent envelope {} to this world's mailbox", envelope);
        } else {
            // The message is destined to another server. Send it on the network!
            outsideSender.send(envelope);
        }
    }

    /// Called by [OutsideReceiver] to receive envelopes coming from the network.
    void receive(Envelope<?> envelope) {
        mailbox.add(envelope);
    }

    @Scheduled(fixedRate = 1000) // todo: configurable rate
    private void cleanupTimedOutRequests() {
        record Entry(long id, PendingRequest request) { }

        var expiredRequests = new ArrayList<Entry>();
        var now = Instant.now();
        pendingRequests.forEach((id, request) -> {
            if (request.timeoutAt().isBefore(now)) {
                expiredRequests.add(new Entry(id, request));
            }
        });

        for (Entry entry : expiredRequests) {
            pendingRequests.remove(entry.id);
        }

        for (Entry entry : expiredRequests) {
            entry.request.future.completeExceptionally(new TimeoutException());
        }
    }

    record PendingRequest(CompletableFuture<?> future, Instant timeoutAt) { }
}
