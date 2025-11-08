package cy.cav.framework;

import java.util.concurrent.*;
import java.util.function.*;

/// An Actor exists in a [World], receiving and sending messages through its lifetime.
///
/// ## What you can do with it
///
/// - react to incoming messages by overriding [#process(Envelope)]
/// - do stuff when spawning by overriding [#spawned()]
/// - do stuff when despawning by overriding [#despawned()]
/// - despawn yourself with [#despawn()]
/// - send messages with [#send(ActorAddress, Message)]
/// - know your address with [#address]
/// - know the world you're in with [#world] (which allows to spawn actors...)
public abstract class Actor {
    /// The world this actor is in.
    protected final World world;
    /// The address for this actor.
    protected final ActorAddress address;
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

    /// Sends a message to another actor. Doesn't wait for its response.
    ///
    /// @param receiver the actor to send the message to
    /// @param body     the body of the message
    protected final void send(ActorAddress receiver, Message body) {
        world.send(address, receiver, body);
    }

    protected final <T extends Message> CompletionStage<T> query(ActorAddress receiver, Message.WithResponse<T> body) {
        return world.query(address, receiver, body);
    }

    protected final void respond(Envelope<?> envelope, Message body) {
        world.respond(address, envelope, body);
    }

    /// Called when the actor receives a message.
    ///
    /// This method is guaranteed to always be called on the same thread.
    ///
    /// @param envelope the envelope containing the message
    protected abstract void process(Envelope<?> envelope);

    /// Called by [World] only to receive messages.
    void acceptEnvelope(Envelope envelope) {
        process(envelope);
    }

    /// The current state of this actor.
    public ActorState state() {
        return state;
    }
}

