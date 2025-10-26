package cy.cav.framework;

/// The state of an [Actor] in the [World].
public enum ActorState {
    /// The actor has been created, but not yet added to the world.
    DETACHED,
    /// The actor has been registered to the world, actively listening for messages.
    ALIVE,
    /// The actor has been removed from the world.
    DEAD
}
