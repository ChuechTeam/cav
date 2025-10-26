package cy.cav.framework;

/// An Actor exists in a [World], receiving and sending messages through its lifetime.
///
/// ## What you can do with it
///
/// - react to incoming messages by overriding [#process(Envelope)]
/// - do stuff when spawning by overriding [#spawned()]
/// - do stuff when despawning by overriding [#despawned()]
/// - despawn yourself with [#despawn()]
/// - send messages with [#send(cy.cav.framework.ActorId, cy.cav.framework.Message)]
/// - know your id with [#id]
/// - know the world you're in with [#world] (which allows to spawn actors...)
public abstract class Actor {
    /// The world this actor is in.
    protected final World world;
    /// The identifier of this actor.
    protected final ActorId id;
    private ActorState state;

    /// Prepares the Actor to be added in a [World] by accepting a [ActorInit] object,
    /// giving us the actor's id and world.
    ///
    /// An Actor created with this constructor has a state of [ActorState#DETACHED]
    ///
    /// @param init the object containing world and id data
    protected Actor(ActorInit init) {
        this.id = init.id();
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
    protected void spawned() {}

    /// Called when this actor has been despawned and removed from the world.
    protected void despawned() {}

    /// Despawns this actor. Calls [#despawned()].
    ///
    /// Does nothing if the actor isn't [alive][ActorState#ALIVE].
    protected final void despawn() {
        if (state == ActorState.ALIVE) {
            world.despawn(id);
        }
    }

    /// Sends a message to another actor. Doesn't wait for its response.
    ///
    /// @param recipient the actor to send the message to
    /// @param body      the body of the message
    protected final void send(ActorId recipient, Message body) {
        world.send(id, recipient, body);
    }

    /// Called when the actor receives a message.
    ///
    /// This method is guaranteed to always be called on the same thread.
    ///
    /// @param envelope the envelope containing the message
    protected abstract void process(Envelope envelope);

    /// Called by [World] only to receive messages.
    void acceptEnvelope(Envelope envelope) {
        process(envelope);
    }

    /// The current state of this actor.
    public ActorState state() {
        return state;
    }
}

