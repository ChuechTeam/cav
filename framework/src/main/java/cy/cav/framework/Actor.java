package cy.cav.framework;

import java.util.*;
import java.util.concurrent.*;

public abstract class Actor {
    protected final World world;
    protected final ActorId id;

    protected Actor(ActorInit init) {
        this.id = init.id();
        this.world = init.world();
    }

    protected void despawn() {
        world.despawn(id);
    }

    protected void spawned() {
    }

    protected void despawned() {
    }

    protected abstract void process(Object message);

    void acceptMessage(Object message) {
        process(message);
    }
}

