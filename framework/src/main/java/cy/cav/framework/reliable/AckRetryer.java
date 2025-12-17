package cy.cav.framework.reliable;

import cy.cav.framework.*;
import cy.cav.framework.Timer;
import jakarta.annotation.*;

import java.time.*;
import java.util.*;

/// Sends messages over and over until the receiver responds with an acknowledgment.
public class AckRetryer {
    private final Map<UUID, PendingMessage<?>> pendingMessages = new HashMap<>();
    private final Actor actor;
    private final DelayFunction delayFunction;
    private int maxRetries;

    /// Creates an [AckRetryer] with the given delay function and a default max retry count of 100.
    ///
    /// @param actor         the actor who's going to send messages
    /// @param delayFunction a function that takes in the number of retries (starts by one) and returns a delay
    public AckRetryer(Actor actor, DelayFunction delayFunction) {
        this(actor, delayFunction, 100);
    }

    /// Creates an [AckRetryer] with the given delay function and a max retry count.
    ///
    /// @param actor         the actor who's going to send messages
    /// @param delayFunction a function that takes in the number of retries (starts by one) and returns a delay,
    ///                                            a negative value will stop retrying this message forever
    /// @param maxRetries    the maximum number of retries before giving up (at least 1 retry will be done regardless)
    public AckRetryer(Actor actor, DelayFunction delayFunction, int maxRetries) {
        this.actor = actor;
        this.delayFunction = delayFunction;
        this.maxRetries = maxRetries;
    }

    /// Creates an [AckRetryer] that will retry messages every X seconds/minutes/whatever
    public static AckRetryer constantDelay(Actor actor, Duration delay) {
        return new AckRetryer(actor, _ -> delay);
    }

    /// Creates an [AckRetryer] that will retry messages with a delay of `increment * retryCount + baseDelay`
    public static AckRetryer additiveDelay(Actor actor, Duration baseDelay, Duration increment) {
        return new AckRetryer(actor, retryCount -> baseDelay.plus(increment.multipliedBy(retryCount)));
    }

    /// Sends a message to an actor, trying to send it over and over until the actor responds with an acknowledgment.
    public <T extends Message.Notification & Acknowledgeable> void send(ActorAddress receiver, T message) {
        send(_ -> receiver, message);
    }

    /// Sends a message to an actor, trying to send it over and over until the actor responds with an acknowledgment.
    public <T extends Message.Notification & Acknowledgeable> void send(ActorFunction receiverFunction, T message) {
        if (pendingMessages.containsKey(message.ackId())) {
            throw new IllegalStateException("Message with id " + message.ackId() + " is already pending!");
        }

        // Save the pending message. (retryCount = 1 since we're going to send the message once)
        PendingMessage<T> pendingMessage = new PendingMessage<>(message, receiverFunction, 1);
        pendingMessages.put(message.ackId(), pendingMessage);

        // Send a first attempt.
        ActorAddress receiver = receiverFunction.choose(0);
        if (receiver != null) {
            actor.send(receiver, message);
        }

        // Schedule the retry.
        Duration delay = delayFunction.compute(pendingMessage.retryCount);
        pendingMessage.retryTimer = actor.sendDelayed(actor.address(), new RetrySend(message.ackId()), delay);
    }

    /// After a while, sends a message to an actor, trying to send it over and over
    /// until the actor responds with an acknowledgment.
    public <T extends Message.Notification & Acknowledgeable> void sendDelayed(ActorAddress receiver, T message, Duration initialDelay) {
        sendDelayed(_ -> receiver, message, initialDelay);
    }

    /// After a while, sends a message to an actor, trying to send it over and over
    /// until the actor responds with an acknowledgment.
    public <T extends Message.Notification & Acknowledgeable> void sendDelayed(ActorFunction receiverFunction, T message, Duration initialDelay) {
        if (pendingMessages.containsKey(message.ackId())) {
            throw new IllegalStateException("Message with id " + message.ackId() + " is already pending!");
        }

        // Save the pending message. (retryCount = 0 since we're not going to send the message yet)
        PendingMessage<T> pendingMessage = new PendingMessage<>(message, receiverFunction, 0);
        pendingMessages.put(message.ackId(), pendingMessage);

        // Schedule the retry with the same duration as the initial delay.
        pendingMessage.retryTimer = actor.sendDelayed(actor.address(), new RetrySend(message.ackId()), initialDelay);
    }

    public void giveUp(UUID ackId) {
        pendingMessages.remove(ackId);
    }

    /// Processes incoming acknowledgments and retries.
    ///
    /// @return true when the actor should stop processing this message; false when it should continue processing it
    public boolean process(Envelope<?> envelope) {
        if (envelope.body() instanceof Acknowledgeable acknowledgeable) {
            PendingMessage<?> pendingMessage = pendingMessages.remove(acknowledgeable.ackId());
            if (pendingMessage != null) {
                pendingMessage.retryTimer.cancel();
            }
            return false;
        }

        if (envelope.body() instanceof RetrySend retrySend) {
            PendingMessage<?> pendingMessage = pendingMessages.getOrDefault(retrySend.id, null);
            if (pendingMessage == null) {
                // Probably a timer that was canceled too late
                return true;
            }

            // Send the message again
            ActorAddress receiver = pendingMessage.receiverFunction.choose(pendingMessage.retryCount);
            if (receiver != null) {
                actor.send(receiver, pendingMessage.message);
            }

            // When we have too much retries, give up.
            pendingMessage.retryCount++;
            if (pendingMessage.retryCount > maxRetries) {
                pendingMessages.remove(retrySend.id);
                return true;
            }

            // When we have a negative delay, give up.
            Duration delay = delayFunction.compute(pendingMessage.retryCount);
            if (delay.isNegative()) {
                pendingMessages.remove(retrySend.id);
                return true;
            }

            // Otherwise, reschedule the retry.
            pendingMessage.retryTimer = actor.sendDelayed(actor.address(), retrySend, delay);
            return true;
        }

        return false;
    }

    public AckRetryer maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    private static class PendingMessage<T extends Message.Notification & Acknowledgeable> {
        T message;
        ActorFunction receiverFunction;
        int retryCount;
        Timer retryTimer;

        public PendingMessage(T message, ActorFunction receiverFunction, int retryCount) {
            this.message = message;
            this.receiverFunction = receiverFunction;
            this.retryCount = retryCount;
        }
    }

    private record RetrySend(UUID id) implements Message.Notification { }

    /// A function that takes in the count of retries and returns how much time it should wait.
    @FunctionalInterface
    public interface DelayFunction {
        /// Returns how much time to wait before sending the same message again, given the retry count.
        ///
        /// A negative duration will stop retrying this message forever.
        ///
        /// @param retryCount the number of this retry attempt (starts by 1)
        /// @return how much time to wait; negative means stop retrying
        Duration compute(int retryCount);
    }

    @FunctionalInterface
    public interface ActorFunction {
        @Nullable
        ActorAddress choose(int retryCount);
    }
}
