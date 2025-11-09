package cy.cav.framework;

import java.util.concurrent.*;

/// An Actor exists in a [World], receiving and sending messages through its lifetime.
///
/// ## What you can do with it
///
/// - react to incoming messages by overriding [#process(Envelope)]
/// - do stuff when spawning by overriding [#spawned()]
/// - do stuff when despawning by overriding [#despawned()]
/// - despawn yourself with [#despawn()]
/// - send notifications with [#send(ActorAddress, Message.Notification)]
/// - send requests with [#send(ActorAddress, Message.Request)]
/// - know your address with [#address]
/// - know the world you're in with [#world] (which allows to spawn actors...)
public abstract class Actor {
    /// The world this actor is in.
    protected final World world;
    /// The address for this actor.
    protected final ActorAddress address;
    /// The state of this actor.
    private volatile ActorState state;

    /// Prepares the Actor to be added in a [World] by accepting a [ActorInit] object,
    /// giving us the actor's address and world.
    ///
    /// An Actor created with this constructor has a state of [ActorState#DETACHED]
    ///
    /// @param init the object containing world and address data
    protected Actor(ActorInit init) {
        this.address = init.address();
        this.world = init.world();
        this.state = ActorState.DETACHED;
    }

    /// Called by [World] only.
    void reportSpawned() {
        state = ActorState.ALIVE;
        spawned();
    }

    /// Called by [World] only.
    void reportDespawned() {
        state = ActorState.DEAD;
        despawned();
    }

    /// Called when this actor has been spawned and added to the world.
    protected void spawned() { }

    /// Called when this actor has been despawned and removed from the world.
    protected void despawned() { }

    /// Despawns this actor. Calls [#despawned()].
    ///
    /// Does nothing if the actor isn't [alive][ActorState#ALIVE].
    protected final void despawn() {
        if (state == ActorState.ALIVE) {
            world.despawn(address.actorNumber());
        }
    }

    /// Sends a **notification** to an actor.
    ///
    /// Notifications are NOT guaranteed to be sent to the receiver.
    ///
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    public final void send(ActorAddress receiver, Message.Notification body) {
        world.send(address, receiver, body);
    }

    /// Sends a **request** to an actor and **doesn't care about its response**.
    ///
    /// Requests are NOT guaranteed to be sent to the destination actor.
    ///
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    public final void send(ActorAddress receiver, Message.Request<?> body) {
        world.send(address, receiver, body);
    }

    /// Sends a **request** to an actor, **waiting for its response** in a [CompletionStage].
    ///
    /// The [CompletionStage] will be complete:
    /// - successfully, when the receiver responds to this request
    /// - unsuccessfully, when the receiver takes too long to respond (30 seconds) or doesn't know how to handle the request (TODO)
    ///
    /// The [CompletionStage] will complete on a thread that makes it safe to change this actor's state.
    ///
    /// The [CompletionStage] will NEVER complete if this actor is dead once the request ends.
    ///
    /// Requests are NOT guaranteed to be sent to the destination actor.
    ///
    /// @param receiver the id of the actor to send the message to
    /// @param body     the body of the message
    /// @return a [CompletionStage] which will complete successfully once the actor responds properly, or with a failure
    ///         when the actor fails to respond after a certain amount of time
    public final <T extends Message.Response> CompletionStage<T> query(ActorAddress receiver, Message.Request<T> body) {
        return world.query(address, receiver, body);
    }

    /// Responds to a request described by the given envelope.
    ///
    /// Make sure the type of the response matches the type expected by the request! Otherwise, errors may
    /// happen on the receiver.
    ///
    /// Does nothing if the envelope doesn't have a request id.
    ///
    /// @param envelope the envelope containing the request
    /// @param body     the response message to send
    public final void respond(Envelope<?> envelope, Message.Response body) {
        world.respond(address, envelope, body);
    }

    /// Called when the actor receives an envelope.
    ///
    /// This method is guaranteed to always be called on the same thread.
    ///
    /// **IMPORTANT: Avoid running long-lasting operations in this method.**
    /// Things like file system operations, sleeping, waiting should be done either:
    /// - with asynchronous APIs
    /// - on another thread
    ///
    /// Otherwise, the whole world stops processing messages because one actor is taking ages processing
    /// its messages!
    ///
    /// @param envelope the envelope containing the message
    protected abstract void process(Envelope<?> envelope);

    /// Called by [World] only to receive messages. Later on we'll have extra logic here.
    void acceptEnvelope(Envelope<?> envelope) {
        // Don't accept the message if we aren't alive. That sounds obvious but if this ever happens due to a bug
        // in World... We better be aware of it!
        if (state != ActorState.ALIVE) {
            throw new IllegalStateException("Can't accept envelope while not alive!");
        }

        process(envelope);
    }

    /// The current state of this actor.
    public ActorState state() {
        return state;
    }
}

