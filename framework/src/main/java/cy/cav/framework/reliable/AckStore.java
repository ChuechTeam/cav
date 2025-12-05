package cy.cav.framework.reliable;

import cy.cav.framework.*;
import jakarta.annotation.*;

import java.util.*;
import java.util.function.*;

/// Keeps a history of acknowledged messages with their answers.
///
/// @param <Out> the type of the response message to save and send back if the request is already acknowledged
public class AckStore<Out extends Message.Notification & Acknowledgeable> {
    private static final int MAX_ENTRIES = 10000;

    // List of all requests we've received and processed.
    private final Map<UUID, Out> acknowledgedMessages = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Out> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    private final Actor actor;

    public AckStore(Actor actor) { this.actor = actor; }

    /// Registers a response message as an acknowledgment to a request.
    public void add(Out message) {
        acknowledgedMessages.put(message.ackId(), message);
    }

    /// Gets the response message associated with the acknowledgment id.
    public @Nullable Out get(UUID ackId) {
        return acknowledgedMessages.getOrDefault(ackId, null);
    }

    /// Gets the response message associated with the acknowledgment id contained in the given message.
    public @Nullable Out get(Acknowledgeable msg) {
        return get(msg.ackId());
    }

    /// Sends a notification to an actor and saves the message as an acknowledged one.
    public void send(ActorAddress receiver, Out message) {
        add(message);
        actor.send(receiver, message);
    }

    /// Sends the saved message to the sender only if the request contained in the envelope
    /// has already been acknowledged.
    ///
    /// @return true when the response was sent, false otherwise
    public <In extends Message & Acknowledgeable> boolean sendIfAcknowledged(Envelope<In> request) {
        return sendIfAcknowledged(request.sender(), request.body());
    }

    /// Sends the saved message to the receiver only if the request has already been acknowledged.
    ///
    /// @return true when the response was sent, false otherwise
    public boolean sendIfAcknowledged(ActorAddress receiver, Acknowledgeable request) {
        Out existing = get(request);
        if (existing == null) { return false; }

        actor.send(receiver, existing);
        return true;
    }
}
