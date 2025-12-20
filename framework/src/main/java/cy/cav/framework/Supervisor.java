package cy.cav.framework;

import org.slf4j.*;

import java.util.*;

/// Takes over an actor's decision-making when it encounters an error while processing a message.
///
/// The [#handle(Exception, Envelope)] method is used to know what to do when encountering a specific error.
///
/// The [#process(Envelope)] method is called when the supervisor is attached to the actor, meaning it has
/// taken control of the actor temporarily.
public abstract class Supervisor {
    private static final Logger log = LoggerFactory.getLogger(Supervisor.class);

    /// The actor this supervisor... supervises.
    protected final Actor actor;
    /// The world this supervisor's actor is in.
    protected final World world;
    /// The stash of envelopes to send after being detached from the actor.
    ///
    /// Is emptied after every detachment.
    protected final List<Envelope<?>> stash = new ArrayList<>();

    protected Supervisor(Actor actor) {
        this.actor = actor;
        this.world = actor.world;
    }

    /// Returns what the supervisor should do when it sees a particular error.
    ///
    /// @param e the error we've encountered...
    /// @param envelope ...while processing this envelope
    protected abstract HandleAction handle(Exception e, Envelope<?> envelope);

    /// Processes the message received by the actor while this supervisor is attached.
    ///
    /// Can detach or stay attached afterward.
    ///
    /// @param envelope the envelope the actor received, that this supervisor processes instead of the actor
    protected abstract ProcessAction process(Envelope<?> envelope);

    /// Runs the actor's [Actor#process(cy.cav.framework.Envelope)] method with the given envelope.
    ///
    /// Use this method carefully!
    protected final void actorProcess(Envelope<?> envelope) {
        actor.process(envelope);
    }

    /// Called by [Actor] when it has detached the supervisor so it can send messages to World
    /// when the actor is in now in the ALIVE state.
    void flushStash() {
        stash.forEach(world::receive);
        stash.clear();
    }

    public enum HandleAction {
        /// Ignore the message and let the actor continue living normally.
        IGNORE,
        /// Take over control of the actor and start processing messages instead of running its own behavior.
        ATTACH
    }

    public enum ProcessAction {
        /// Stop processing the actor's messages and send any [stashed messages][Supervisor#stash].
        DETACH,
        /// Keep processing the actor's messages.
        STAY_ATTACHED,
        /// Despawn the actor entirely.
        DESPAWN
    }

    /// Logs message processing failures and continues as if nothing happened.
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
