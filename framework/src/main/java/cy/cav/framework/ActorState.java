package cy.cav.framework;

/// The state of an [Actor] in the [World].
public enum ActorState {
    /// The actor has been created, but not yet added to the world.
    DETACHED(false),
    /// The actor has been registered to the world, actively listening for messages.
    ALIVE(true),
    /// The supervisor is taking control of this actor.
    SUPERVISED(true),
    /// The actor has been removed from the world.
    DEAD(false);

    private final boolean active;

    ActorState(boolean active) { this.active = active; }

    /// Returns true when the actor having this state is present in the world.
    public boolean active() {
        return active;
    }
}
