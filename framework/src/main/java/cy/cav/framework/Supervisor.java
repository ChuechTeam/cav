package cy.cav.framework;

import org.slf4j.*;

import java.util.*;

public abstract class Supervisor {
    private static final Logger log = LoggerFactory.getLogger(Supervisor.class);

    protected final Actor actor;

    protected Supervisor(Actor actor) { this.actor = actor; }

    protected abstract HandleAction handle(Exception e, Envelope<?> envelope);

    protected void attached() { }

    protected abstract ProcessAction process(Envelope<?> envelope);

    protected void detached() { }

    public enum HandleAction {
        IGNORE,
        ATTACH
    }

    public sealed interface ProcessAction {
        record Detach(List<Envelope<?>> envelopesToProcess) implements ProcessAction {
            public Detach {
                Objects.requireNonNull(envelopesToProcess);
            }
        }

        record StayAttached() implements ProcessAction { }
    }

    public static final class Default extends Supervisor {
        public Default(Actor actor) {
            super(actor);
        }

        @Override
        protected HandleAction handle(Exception e, Envelope<?> envelope) {
            log.error("Exception happened while actor {} is processing envelope", actor, e);
            return HandleAction.IGNORE;
        }

        @Override
        protected ProcessAction process(Envelope<?> envelope) {
            return new ProcessAction.Detach(List.of());
        }
    }
}
