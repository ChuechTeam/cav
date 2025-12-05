package cy.cav.framework.reliable;

import java.util.*;

/// A piece of data with an acknowledgment id.
public interface Acknowledgeable {
    /// A unique identifier for a request/response conversation.
    UUID ackId();
}
