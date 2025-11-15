package cy.cav.client.actors;

import cy.cav.framework.*;
import cy.cav.protocol.KnownActors;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.demandes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * Proxy actor in client that forwards requests to the service.
 * This actor serves as an intermediary to retrieve data from the service.
 * 
 * Acteur proxy dans le client qui transmet les requêtes au service.
 * Cet acteur sert d'intermédiaire pour récupérer les données depuis le service.
 */
public class AllocataireProxy extends Actor {
    private static final Logger log = LoggerFactory.getLogger(AllocataireProxy.class);
    
    private final Network network;
    
    static final Router<AllocataireProxy> router = new Router<AllocataireProxy>()
        .route(CreateAccountRequest.class, AllocataireProxy::createAccount)
        .route(GetAccountRequest.class, AllocataireProxy::getAccount)
        .route(CheckAccountExistsRequest.class, AllocataireProxy::checkAccountExists)
        .route(CreateDemandeRequest.class, AllocataireProxy::createDemande)
        .route(UpdateDemandeRequest.class, AllocataireProxy::updateDemande)
        .route(GetDemandeRequest.class, AllocataireProxy::getDemande);
    
    public AllocataireProxy(ActorInit init, Network network) {
        super(init);
        this.network = network;
    }
    
    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }
    
    /**
     * Forwards create account request to service.
     * Transmet la demande de création de compte au service.
     */
    CreateAccountResponse createAccount(Envelope<CreateAccountRequest> envelope) {
        log.info("Proxy: Transmission de la demande de création de compte au service");
        forwardToServiceAsync(envelope, KnownActors.GESTIONNAIRE_COMPTE);
        return null; // Réponse asynchrone
    }
    
    /**
     * Forwards get account request to service.
     * Transmet la demande de récupération de compte au service.
     */
    GetAccountResponse getAccount(Envelope<GetAccountRequest> envelope) {
        log.debug("Proxy: Transmission de la demande de récupération de compte au service");
        forwardToServiceAsync(envelope, KnownActors.GESTIONNAIRE_COMPTE);
        return null; // Réponse asynchrone
    }
    
    /**
     * Forwards check account exists request to service.
     * Transmet la demande de vérification d'existence au service.
     */
    CheckAccountExistsResponse checkAccountExists(Envelope<CheckAccountExistsRequest> envelope) {
        log.debug("Proxy: Transmission de la demande de vérification d'existence au service");
        forwardToServiceAsync(envelope, KnownActors.GESTIONNAIRE_COMPTE);
        return null; // Réponse asynchrone
    }
    
    CreateDemandeResponse createDemande(Envelope<CreateDemandeRequest> envelope) {
        log.info("Proxy: Transmission de la demande de création de demande au service");
        forwardToServiceAsync(envelope, KnownActors.GESTIONNAIRE_DEMANDES);
        return null; // Réponse asynchrone
    }
    
    UpdateDemandeResponse updateDemande(Envelope<UpdateDemandeRequest> envelope) {
        log.debug("Proxy: Transmission de la mise à jour de demande au service");
        forwardToServiceAsync(envelope, KnownActors.GESTIONNAIRE_DEMANDES);
        return null; // Réponse asynchrone
    }
    
    GetDemandeResponse getDemande(Envelope<GetDemandeRequest> envelope) {
        log.debug("Proxy: Transmission de la récupération de demande au service");
        forwardToServiceAsync(envelope, KnownActors.GESTIONNAIRE_DEMANDES);
        return null; // Réponse asynchrone
    }
    
    /**
     * Forwards a request to the service asynchronously.
     * Quand la réponse arrive, on répond au controller avec le requestId original.
     * 
     * Transmet une requête au service de manière asynchrone.
     * Quand la réponse arrive, on répond au controller avec le requestId original.
     */
    private <T extends Message.Response> void forwardToServiceAsync(Envelope<? extends Message.Request<T>> originalEnvelope, Long actorId) {
        // Find service via Network (Eureka)
        Map<Long, Server> servers = network.servers();
        
        // Debug: log all available servers
        if (servers.isEmpty()) {
            log.warn("Aucun serveur disponible dans le réseau. Le service cav-service n'est peut-être pas encore démarré ou enregistré dans Eureka.");
            // Ne pas répondre, laisser timeout (on ne peut pas créer de réponse d'erreur générique)
            return;
        }
        
        Optional<Server> serviceServer = servers.values().stream()
            .filter(server -> server.appName().equalsIgnoreCase("cav-service"))
            .findFirst();
        
        if (serviceServer.isEmpty()) {
            log.error("Service cav-service non trouvé via Eureka. Serveurs disponibles: {}", 
                servers.values().stream()
                    .map(Server::appName)
                    .toList());
            // Ne pas répondre, laisser timeout (on ne peut pas créer de réponse d'erreur générique)
            return;
        }
        
        // Get actor address
        Server server = serviceServer.get();
        ActorAddress actorAddress = server.address(actorId);
        
        log.debug("Envoi de la requête vers acteur {} sur serveur {}: {}", 
            actorId, server.appName(), originalEnvelope.body().getClass().getSimpleName());
        
        // Send request asynchronously - query() génère un nouveau requestId
        // Utiliser null comme sender pour que la Response arrive avec receiver=server.address()
        // et soit reconnue par le World comme une Response (pas une Notification)
        // Quand la réponse arrive, on répondra au controller avec le requestId original
        Message.Request<T> request = (Message.Request<T>) originalEnvelope.body();
        world.query(null, actorAddress, request)
            .thenAccept(response -> {
                // Répondre au controller avec le requestId original
                respond(originalEnvelope, response);
            })
            .exceptionally(throwable -> {
                log.error("Erreur lors de la transmission de la requête au service", throwable);
                // Ne pas répondre, laisser timeout (on ne peut pas créer de réponse d'erreur générique)
                return null;
            });
    }
    
}

