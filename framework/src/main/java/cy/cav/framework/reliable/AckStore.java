package cy.cav.framework.reliable;

import cy.cav.framework.*;
import jakarta.annotation.*;

import java.util.*;

public class AckStore<T extends Message & Acknowledgeable> {
    private static final int MAX_ENTRIES = 10000;

    // List of all requests we've received and processed.
    private final Map<UUID, T> acknowledgedMessages = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, T> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public void add(T message) {
        acknowledgedMessages.put(message.ackId(), message);
    }

    public @Nullable T get(UUID ackId) {
        return acknowledgedMessages.getOrDefault(ackId, null);
    }

    public @Nullable T get(Acknowledgeable msg) {
        return get(msg.ackId());
    }
}
