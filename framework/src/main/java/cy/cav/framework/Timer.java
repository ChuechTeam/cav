package cy.cav.framework;

import jakarta.annotation.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/// A message sending task that will be done after a period of time.
///
/// Use [#cancel()] to cancel it at any time.
public class Timer {
    // Filled later with setFuture, otherwise we might have a race condition with an unregistered timer
    // already done!
    private ScheduledFuture<?> future;
    private final Consumer<Timer> unregisterCallback;

    Timer(Consumer<Timer> unregisterCallback) {
        this.unregisterCallback = Objects.requireNonNull(unregisterCallback);
    }

    /// Internally used by this class and [World] to unregister the timer once the message has been sent.
    void unregister() {
        unregisterCallback.accept(this);
    }

    /// Internally used by [World] to fill in the scheduled future.
    void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    /// Used internally by [Actor] to cancel the future when destroying all actors.
    void cancelWithoutUnregistering() {
        if (future == null) {
            return;
        }

        future.cancel(false);
    }

    /// Cancels this timer, preventing it from sending the message at a later date.
    ///
    /// If the message has already been sent, does nothing.
    public void cancel() {
        if (future == null) {
            return;
        }

        if (future.cancel(false)) {
            unregister();
        }
    }
}
