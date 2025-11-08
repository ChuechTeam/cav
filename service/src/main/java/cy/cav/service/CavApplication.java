package cy.cav.service;

import cy.cav.framework.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;

@SpringBootApplication
public class CavApplication implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;

    public CavApplication(World world) {
        this.world = world;
    }

    public static void main(String[] args) {
        SpringApplication.run(CavApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // todo
    }
}
