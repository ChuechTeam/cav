package cy.cav.framework;

import jakarta.annotation.*;
import org.slf4j.*;
import org.springframework.context.*;
import org.springframework.scheduling.*;
import org.springframework.scheduling.annotation.*;

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
/// - send notifications with [#send(ActorAddress, ActorAddress, Message.Notification)]
/// - send requests with [#query(ActorAddress, ActorAddress, Message.Request)]
/// - start processing messages with [#start()]
///
/// ## What you CANNOT do with it
/// - despawn actors; only actors can despawn themselves using [Actor#despawn()]
///
/// @see Actor
public class World implements SmartLifecycle {
    /// Maximum value for a special actor number, exclusive.
    public static final long SPECIAL_ACTOR_NUM_MAX = 66536;

    // Used to write messages in the console with priorities (warning, info, error)
    private static final Logger log = LoggerFactory.getLogger(World.class);

    private final BlockingQueue<Envelope<?>> mailbox = new LinkedBlockingQueue<>();
    private final ConcurrentMap<Long, Actor> actors = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    private final Server server;
    private final OutsideSender outsideSender;
    private final TaskScheduler taskScheduler;

    private volatile boolean running = false;
    private volatile Thread mainLoopThread = null;

    private final AtomicLong nextActorNumber = new AtomicLong(SPECIAL_ACTOR_NUM_MAX);
    private final AtomicLong nextRequestId = new AtomicLong(1);

    /// Creates a new [World]. Called by [Framework].
    World(Server server, OutsideSender outsideSender, TaskScheduler taskScheduler) {
        this.server = Objects.requireNonNull(server);
        this.outsideSender = Objects.requireNonNull(outsideSender);
        this.taskScheduler = Objects.requireNonNull(taskScheduler);
    }

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
                break;
            }

            // Make sure this envelope is actually destined for this server. Else it doesn't make any sense.
            if (envelope.receiver().serverId() != server.id()) {
                log.warn("Somehow an envelope with the wrong server id made its way into this world's mailbox: {}", envelope);
                continue;
            }

            // See if this envelope is destined for this server, in which case we're processing a
            // response to a request.
            if (envelope.receiver().isServerAddress()) {
                // Find the request this envelope responds to.
                PendingRequest request = pendingRequests.remove(envelope.requestId());
                if (request == null) {
                    log.warn("Unknown request {} for response given by envelope: {}", envelope.requestId(), envelope);
                    continue;
                }

                // If an actor sent this request, we need to make sure it's still alive!
                // Otherwise, ignore the request.
                if (request.senderActorNum != 0 && !actors.containsKey(request.senderActorNum)) {
                    log.info("Received response envelope for request {}, but sender is dead! {}", envelope.requestId(), envelope);
                    continue;
                }

                // Complete the future with the message contained inside the envelope.
                log.debug("Received response envelope for request {}: {}", envelope.requestId(), envelope);
                try {
                    var future = (CompletableFuture<Object>) request.future;
                    if (envelope.body() instanceof ActorNotFoundResponse(ActorAddress address)) {
                        future.completeExceptionally(new ActorNotFoundException("Failed to find actor " + address));
                    } else {
                        future.complete(envelope.body());
                    }
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
                log.debug("Received envelope for actor {}: {}", receiver, envelope);
                try {
                    receiver.acceptEnvelope(envelope);
                } catch (Exception e) {
                    // In case the supervisor doesn't do its work properly...
                    log.error("Exception occured while actor {} is processing envelope {}", receiver, envelope, e);
                }
            } else {
                // Then the actor is either:
                // - dead
                // - not an actor we know about
                //
                // So just ignore it! But if it's a request, then we need to tell them that the actor doesn't exist.
                log.warn("Received envelope for an unknown actor: {}", envelope);
                if (envelope.requestId() != 0) {
                    respond(server.address(), envelope, new ActorNotFoundResponse(envelope.receiver()));
                }
            }
        }

        // The main loop has ended; it's time to destroy all actors.
        log.info("Main loop ended; despawning all actors...");
        for (Actor actor : actors.values()) {
            actor.reportDespawned();
        }
    }

    /// Spawns a new actor using the given creator function, with a generated actor number.
    ///
    /// The creator function accepts an [ActorInit] object, containing the id of the new actor (among other things),
    /// and returns an [Actor].
    ///
    /// @param creator the creator function
    /// @return the address of the created actor
    public ActorAddress spawn(Function<ActorInit, Actor> creator) {
        return spawn(creator, 0, null);
    }

    public ActorAddress spawn(Function<ActorInit, Actor> creator, Function<Actor, Supervisor> supervisorCreator) {
        return spawn(creator, 0, supervisorCreator);
    }

    public ActorAddress spawn(Function<ActorInit, Actor> creator, long specialNumber) {
        return spawn(creator, specialNumber, null);
    }

    /// Spawns a new actor using the given creator function, using a custom or generated actor number.
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
    /// @param creator       the creator function
    /// @param specialNumber the special actor number to use; 0 will generate a unique actor number
    /// @return the address of the created actor
    public ActorAddress spawn(Function<ActorInit, Actor> creator, long specialNumber,
                              @Nullable Function<Actor, Supervisor> supervisorCreator) {
        if (specialNumber < 0 || specialNumber >= SPECIAL_ACTOR_NUM_MAX) {
            throw new IllegalArgumentException("Invalid special number: " + specialNumber);
        }

        // Pick the next actor number by incrementing the global counter,
        // and use the serverId to make the final ActorId.
        long actorNumber = specialNumber != 0 ? specialNumber : nextActorNumber.getAndIncrement();
        ActorAddress id = new ActorAddress(server.id(), actorNumber);

        // Create the actor using the function the user gave us. Give it the id we made up.
        Actor actor = creator.apply(new ActorInit(this, id));
        Objects.requireNonNull(actor, "The created actor is null!");

        // Register the actor in the map of existing actors and let it know that we've spawned it.
        Actor existing = actors.putIfAbsent(id.actorNumber(), actor);
        if (existing != null) {
            throw new IllegalStateException("An actor with the same number already exists! " + actorNumber);
        }
        // todo: what if this throws an exception? + possible race condition
        actor.reportSpawned(supervisorCreator != null ? supervisorCreator.apply(actor) : null);

        log.debug("New actor of id {} spawned: {}", id, actor);

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
            log.debug("Actor number {} despawned: {}", despawnedActor.address, despawnedActor);
        }
    }

    /// Sends a **notification** to an actor.
    ///
    /// Notifications are NOT guaranteed to be sent to the receiver.
    ///
    /// @param sender   the actor that sent the message; can be null
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    public void send(@Nullable ActorAddress sender, ActorAddress receiver, Message.Notification body) {
        // Put the message in an envelope, so the postman "knows" which actor to send the message to.
        sender = sender != null ? sender : server.address();
        var envelope = new Envelope<>(sender, receiver, 0, body, Instant.now());
        sendEnvelope(envelope, true);
    }

    /// Sends a **request** to an actor and **doesn't care about its response**.
    ///
    /// Requests are NOT guaranteed to be sent to the destination actor.
    ///
    /// @param sender   the actor that sent the message; can be null
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    public void send(@Nullable ActorAddress sender, ActorAddress receiver, Message.Request<?> body) {
        // Put the message in an envelope, so the postman "knows" which actor to send the message to.
        sender = sender != null ? sender : server.address();
        var envelope = new Envelope<>(sender, receiver, 0, body, Instant.now());
        sendEnvelope(envelope, true);
    }

    /// Sends a **request** to an actor, **waiting for its response** in a [CompletionStage].
    ///
    /// The [CompletionStage] will be complete:
    /// - successfully, when the receiver responds to this request
    /// - unsuccessfully, when the receiver takes too long to respond (30 seconds) or doesn't know how to handle the request (TODO)
    ///
    /// The [CompletionStage] will always complete on the World main loop thread.
    ///
    /// The [CompletionStage] will NEVER complete if the sender actor is dead once the request ends. Note that this
    /// applies only if the sender address is given and has the same server id as this world's server.
    ///
    /// Requests are NOT guaranteed to be sent to the destination actor.
    ///
    /// @param sender   the actor that sent the message; can be null
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    /// @return a [CompletionStage] which will complete successfully once the actor responds properly, or with a failure
    ///         when the actor fails to respond after a certain amount of time
    public <T extends Message.Response> CompletionStage<T> query(@Nullable ActorAddress sender,
                                                                 ActorAddress receiver,
                                                                 Message.Request<T> body) {
        return query(sender, receiver, body, true);
    }

    /// Sends a **request** to an actor, **waiting for its response** in a [CompletionStage].
    ///
    /// The [CompletionStage] will be complete:
    /// - successfully, when the receiver responds to this request
    /// - unsuccessfully, when the receiver takes too long to respond (30 seconds) or doesn't know how to handle the request (TODO)
    ///
    /// The [CompletionStage] will always complete on the World main loop thread.
    ///
    /// The [CompletionStage] will NEVER complete if the sender actor is dead once the request ends. Note that this
    /// applies only if the sender address is given and has the same server id as this world's server.
    ///
    /// Requests are NOT guaranteed to be sent to the destination actor.
    ///
    /// @param sender   the actor that sent the message; can be null
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    /// @param retry    whether to retry sending the message on network errors
    /// @return a [CompletionStage] which will complete successfully once the actor responds properly, or with a failure
    ///         when the actor fails to respond after a certain amount of time
    public <T extends Message.Response> CompletionStage<T> query(@Nullable ActorAddress sender,
                                                                 ActorAddress receiver,
                                                                 Message.Request<T> body,
                                                                 boolean retry) {
        // Create a future for this request, which will complete once we receive the response.
        // TODO: Configurable timeout
        var future = new CompletableFuture<T>();
        long requestId = nextRequestId.getAndIncrement();
        long senderNum = sender != null && sender.serverId() == server.id() ? sender.actorNumber() : 0;
        pendingRequests.put(requestId, new PendingRequest(future, Instant.now().plusSeconds(30), senderNum));

        // Put the message in an envelope, so the postman "knows" which actor to send the message to.
        sender = sender != null ? sender : server.address();
        var envelope = new Envelope<>(sender, receiver, requestId, body, Instant.now());
        sendEnvelope(envelope, retry);

        // Return the future we've created earlier.
        return future;
    }

    /// Sends a **request** to an actor and waits for the response.
    ///
    /// Should not be used in actors!
    ///
    /// Requests are NOT guaranteed to be sent to the destination actor.
    ///
    /// Doesn't retry on network errors for fast API feedback.
    ///
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    /// @throws ActorNotFoundException when the receiver actor does not exist
    public <T extends Message.Response> T querySync(ActorAddress receiver, Message.Request<T> body)
            throws ActorNotFoundException {
        try {
            return query(null, receiver, body, false)
                    .toCompletableFuture()
                    .get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Query sync interrupted", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ActorNotFoundException anfEx) {
                throw anfEx;
            } else {
                throw new RuntimeException("Unknown query sync exception", e);
            }
        }
    }

    /// Called by [Actor] to send a delayed message.
    void sendDelayed(Timer timer, ActorAddress sender, ActorAddress receiver, Message.Notification body, Duration delay) {
        timer.setFuture(taskScheduler.schedule(() -> {
            // Remove the timer from the actor's list of active timers.'
            timer.unregister();
            send(sender, receiver, body);
        }, Instant.now().plus(delay)));
    }

    /// Can only be called by [Actor] or by [World].
    void respond(ActorAddress responder, Envelope<?> envelope, Message.Response body) {
        if (envelope.requestId() == 0) {
            // Don't send the message if this isn't a request. That means the sender doesn't care about our response,
            // not that something's fundamentally wrong or something.
            return;
        }

        // Put the message in an envelope, so the postman "knows" which actor to send the message to.
        var newEnv = new Envelope<>(responder, envelope.sender(), envelope.requestId(), body, Instant.now());
        sendEnvelope(newEnv, true);
    }

    /// Used internally to send an envelope either to this world or to the network.
    private void sendEnvelope(Envelope<?> envelope, boolean retry) {
        if (envelope.receiver().serverId() == server.id()) {
            // The actor we want to send the message to is in this world!
            // Just add the envelope to our local queue.
            mailbox.add(envelope);
            log.debug("Sent envelope {} to this world's mailbox", envelope);
        } else {
            // The message is destined to another server. Send it on the network!
            outsideSender.send(envelope, retry);
        }
    }

    /// Receives an envelope coming from any source, let it be from the network or from somewhere else.
    ///
    /// You can call this method to receive envelopes coming from any service, like a message bus.
    public void receive(Envelope<?> envelope) {
        mailbox.add(envelope);
    }

    /// Returns the server this world runs on.
    public Server server() { return server; }

    // Called every now and then to terminate any pending requests that are pending for way too long.
    @Scheduled(fixedRate = 1000) // todo: configurable rate
    private void cleanupTimedOutRequests() {
        record Entry(long id, PendingRequest request) { }

        // First, identify all requests that have expired.
        var expiredRequests = new ArrayList<Entry>();
        var now = Instant.now();
        pendingRequests.forEach((id, request) -> {
            if (request.timeoutAt().isBefore(now)) {
                expiredRequests.add(new Entry(id, request));
            }
        });

        // Now, remove them all from the map.
        for (Entry entry : expiredRequests) {
            pendingRequests.remove(entry.id);
        }

        // Then, and only after we've cleaned up the map, mark complete the requests with a failure.
        for (Entry entry : expiredRequests) {
            // TODO: QUITE IMPORTANT! Queue completions in the event queue for actor because we're hitting
            //       obvious race conditions!
            entry.request.future.completeExceptionally(new TimeoutException());
        }
    }

    /// A request to an actor to which we're still waiting for its response.
    ///
    /// @param future         the future to complete once we receive the response
    /// @param timeoutAt      the time at which we'll give up and mark the request as failed
    /// @param senderActorNum the actor who started the request; 0 when there's no actor
    record PendingRequest(CompletableFuture<?> future, Instant timeoutAt, long senderActorNum) { }
}
