package cy.cav.service;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.service.actors.GestionnaireCompte;
import cy.cav.service.actors.GestionnaireDemandes;
import cy.cav.service.actors.calculateurs.RSACalculator;
import cy.cav.service.store.AllocationStore;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;

@SpringBootApplication
public class CavApplication implements ApplicationListener<ApplicationStartedEvent> {
    private final World world;
    private final AllocationStore store;

    public CavApplication(World world, AllocationStore store) {
        this.world = world;
        this.store = store;
    }

    public static void main(String[] args) {
        SpringApplication.run(CavApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // Create a new greeter with a special id
        world.spawn(Greeter::new, KnownActors.GREETER);
        
        // Spawn account manager actor (gestionnaire de comptes allocataires)
        world.spawn(init -> new GestionnaireCompte(init, store), KnownActors.GESTIONNAIRE_COMPTE);
        
        // Spawn demand manager actor (gestionnaire de demandes)
        world.spawn(init -> new GestionnaireDemandes(init, store), KnownActors.GESTIONNAIRE_DEMANDES);
        
        // Spawn RSA calculator actor (création de l'acteur calculateur RSA)
        world.spawn(init -> new RSACalculator(init, store), KnownActors.CALCULATEUR_RSA);
    }
}

class Greeter extends Actor {
    public Greeter(ActorInit init) {
        super(init);
    }

    static final Router<Greeter> router = new Router<Greeter>()
            .route(HelloRequest.class, Greeter::greet);

    @Override
    protected void process(Envelope<?> envelope) { router.process(this, envelope); }

    HelloResponse greet(HelloRequest request) {
        return new HelloResponse("Hello " + request.name());
    }
}