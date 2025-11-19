package cy.cav.framework.reliable;

import cy.cav.framework.*;
import cy.cav.framework.Timer;

import java.time.*;
import java.util.*;
import java.util.function.*;

public class AckRetryer {
    private final Map<UUID, PendingMessage<?>> pendingMessages = new HashMap<>();
    private final Actor actor;
    private final IntFunction<Duration> delayFunction;

    public AckRetryer(Actor actor, IntFunction<Duration> delayFunction) {
        this.actor = actor;
        this.delayFunction = delayFunction;
    }

    public <T extends Message.Notification & Acknowledgeable> void send(ActorAddress address, T message) {
        if (pendingMessages.containsKey(message.ackId())) {
            throw new IllegalStateException("Message with id " + message.ackId() + " is already pending!");
        }

        PendingMessage<T> pendingMessage = new PendingMessage<>(message);
        pendingMessages.put(message.ackId(), pendingMessage);
        actor.send(address, message);

        Duration delay = delayFunction.apply(pendingMessage.retryCount);
        pendingMessage.retryTimer = actor.sendDelayed(actor.address(), new RetrySend(message.ackId()), delay);
    }

    public boolean process(Envelope<?> envelope) {
        if (envelope.body() instanceof Acknowledgeable acknowledgeable) {
            PendingMessage<?> pendingMessage = pendingMessages.remove(acknowledgeable.ackId());
            if (pendingMessage != null) {
                pendingMessage.retryTimer.cancel();
            }
            return true;
        }

        if (envelope.body() instanceof RetrySend(UUID id)) {
            PendingMessage<?> pendingMessage = pendingMessages.getOrDefault(id, null);
            if (pendingMessage == null) {
                // Probably a timer that was canceled too late
                return true;
            }

            pendingMessage.retryCount++;
            pendingMessage.retryTimer.cancel(); // just in case
            pendingMessage.retryTimer = actor.sendDelayed(actor.address(), pendingMessage.message, delayFunction.apply(pendingMessage.retryCount));
            return true;
        }

        return false;
    }

    private static class PendingMessage<T extends Message.Notification & Acknowledgeable> {
        T message;
        int retryCount = 1;
        Timer retryTimer;

        public PendingMessage(T message) {
            this.message = message;
        }
    }

    private record RetrySend(UUID id) implements Message.Notification { }
}
