package cy.cav.client;

import cy.cav.client.actors.AllocataireProxy;
import cy.cav.framework.*;
import cy.cav.protocol.KnownActors;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;

@SpringBootApplication
public class ClientApplication implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;
    private final Network network;
    
    public ClientApplication(World world, Network network) {
        this.world = world;
        this.network = network;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
    
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // Spawn AllocataireProxy actor (acteur proxy pour les allocataires)
        world.spawn(init -> new AllocataireProxy(init, network), KnownActors.GESTIONNAIRE_COMPTE);
    }
}