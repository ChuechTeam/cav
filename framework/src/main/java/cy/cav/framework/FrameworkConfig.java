package cy.cav.framework;

import org.springframework.boot.context.properties.*;

import java.util.*;

@ConfigurationProperties(prefix = "cav.framework")
public record FrameworkConfig(Map<String, String> metadata, List<String> applications) {
    public FrameworkConfig {
        metadata = metadata == null ? Map.of() : metadata;
        applications = applications == null ? List.of() : applications;
    }
}
