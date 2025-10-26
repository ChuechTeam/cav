package cy.cav.framework;

/// Data fabricated by the [World] used to create a new actor.
///
/// @param world the world this actor is in
/// @param id    the id assigned to this actor
public record ActorInit(World world, ActorId id) {}
