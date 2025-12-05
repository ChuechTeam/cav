package cy.cav.framework;

import java.util.*;

/// Initializes metadata for the application's server.
///
/// ## Example
///
/// ```java
/// @Component
/// class MyMeta implements MetadataInit {
///     public void populate(long serverId, Map<String, String> metadata) {
///         metadata.put("Bonsoir", "J'ai faim");
///     }
/// }
/// ```
public interface MetadataInit {
    /// Changes the metadata of the server in some way.
    ///
    /// @param serverId the id of the server containing such metadata
    /// @param metadata the metadata to change, initialized with [FrameworkConfig#metadata()] values
    void populate(long serverId, Map<String, String> metadata);
}
