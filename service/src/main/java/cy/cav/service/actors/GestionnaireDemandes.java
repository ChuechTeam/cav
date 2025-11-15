package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.protocol.demandes.*;
import cy.cav.service.domain.DemandeAllocation;
import cy.cav.service.store.AllocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actor that manages allocation demands.
 * All demande data is stored only in the service.
 */
public class GestionnaireDemandes extends Actor {
    private static final Logger log = LoggerFactory.getLogger(GestionnaireDemandes.class);
    
    private final AllocationStore store;
    
    static final Router<GestionnaireDemandes> router = new Router<GestionnaireDemandes>()
        .route(CreateDemandeRequest.class, GestionnaireDemandes::createDemande)
        .route(UpdateDemandeRequest.class, GestionnaireDemandes::updateDemande)
        .route(GetDemandeRequest.class, GestionnaireDemandes::getDemande);
    
    public GestionnaireDemandes(ActorInit init, AllocationStore store) {
        super(init);
        this.store = store;
    }
    
    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }
    
    CreateDemandeResponse createDemande(CreateDemandeRequest request) {
        log.info("Création de demande pour allocataire: {}, type: {}", request.allocataireId(), request.allocationType());
        
        DemandeAllocation demande = new DemandeAllocation(request.allocataireId(), request.allocationType());
        store.saveDemande(demande);
        
        log.info("Demande créée: {} pour allocataire: {}", demande.getId(), request.allocataireId());
        
        return new CreateDemandeResponse(
            demande.getId(),
            demande.getAllocataireId(),
            demande.getAllocationType(),
            demande.getRequestDate(),
            demande.getStatus()
        );
    }
    
    UpdateDemandeResponse updateDemande(UpdateDemandeRequest request) {
        log.debug("Mise à jour de demande: {}", request.demandeId());
        
        return store.findDemandeById(request.demandeId())
            .map(demande -> {
                demande.setStatus(request.status());
                demande.setAllocationId(request.allocationId());
                demande.setMonthlyAmount(request.monthlyAmount());
                demande.setRejectionReason(request.rejectionReason());
                store.saveDemande(demande);
                
                return new UpdateDemandeResponse(true);
            })
            .orElse(new UpdateDemandeResponse(false));
    }
    
    GetDemandeResponse getDemande(GetDemandeRequest request) {
        log.debug("Récupération de demande: {}", request.demandeId());
        
        return store.findDemandeById(request.demandeId())
            .map(demande -> new GetDemandeResponse(
                demande.getId(),
                demande.getAllocataireId(),
                demande.getAllocationType(),
                demande.getRequestDate(),
                demande.getStatus(),
                demande.getAllocationId(),
                demande.getMonthlyAmount(),
                demande.getRejectionReason()
            ))
            .orElseThrow(() -> {
                log.warn("Demande non trouvée: {}", request.demandeId());
                return new RuntimeException("Demande non trouvée");
            });
    }
}

