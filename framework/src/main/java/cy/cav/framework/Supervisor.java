package cy.cav.framework;

import org.slf4j.*;

public abstract class Supervisor {
    private static final Logger log = LoggerFactory.getLogger(Supervisor.class);

    protected final Actor actor;

    protected Supervisor(Actor actor) { this.actor = actor; }

    protected abstract HandleAction handle(Exception e, Envelope<?> envelope);

    protected void attached() {}

    protected abstract ProcessAction process(Envelope<?> envelope);

    protected void detached() {}

    public enum HandleAction {
        IGNORE,
        ATTACH
    }

    public enum ProcessAction {
        STAY_ATTACHED,
        DETACH
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
            return ProcessAction.DETACH;
        }
    }
}
