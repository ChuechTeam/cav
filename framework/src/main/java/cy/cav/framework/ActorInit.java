package cy.cav.framework;

/// Data fabricated by the [World] used to create a new actor.
///
/// @param world   the world this actor is in
/// @param address the address assigned to this actor
public record ActorInit(World world, ActorAddress address) { }
