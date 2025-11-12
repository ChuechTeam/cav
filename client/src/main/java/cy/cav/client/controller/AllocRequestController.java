package cy.cav.client.controller;

import cy.cav.client.domain.DemandeAllocation;
import cy.cav.client.dto.DemandeRSADTO;
import cy.cav.client.dto.DemandeResponse;
import cy.cav.client.store.AllocataireStore;
import cy.cav.framework.*;
import cy.cav.protocol.KnownActors;
import cy.cav.protocol.allocations.CalculateRSARequest;
import cy.cav.protocol.allocations.DecisionAllocationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REST controller for allocation requests (gestion des demandes d'allocation)
@RestController
@RequestMapping("/api/demandes")
public class AllocRequestController {
    private static final Logger log = LoggerFactory.getLogger(AllocRequestController.class);
    
    private final World world;
    private final Network network;
    private final AllocataireStore store;
    
    public AllocRequestController(World world, Network network, AllocataireStore store) {
        this.world = world;
        this.network = network;
        this.store = store;
    }
    
    // Creates RSA allocation request (création d'une demande RSA)
    @PostMapping("/rsa")
    public ResponseEntity<DemandeResponse> demanderRSA(@RequestBody DemandeRSADTO dto) {
        log.info("Demande RSA reçue pour allocataire: {}", dto.allocataireId());
        
        // Check if allocataire exists
        if (!store.existsAllocataire(dto.allocataireId())) {
            log.warn("Allocataire non trouvé: {}", dto.allocataireId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        // Create demande in store
        DemandeAllocation demande = new DemandeAllocation(dto.allocataireId(), "RSA");
        store.saveDemande(demande);
        
        log.info("Demande créée: {} pour allocataire: {}", demande.getId(), dto.allocataireId());
        
        // Debug: log available servers
        log.info("Serveurs disponibles: {}", network.servers().values().stream()
            .map(server -> server.appName() + " (" + server.idString() + ")")
            .toList());
        
        // Find service via Network (Eureka) - Eureka uses uppercase names
        var serviceServer = network.servers().values().stream()
            .filter(server -> server.appName().equalsIgnoreCase("cav-service"))
            .findFirst();
        
        if (serviceServer.isEmpty()) {
            log.error("Service cav-service non trouvé via Eureka. Serveurs disponibles: {}", 
                network.servers().keySet());
            demande.setStatus("REJECTED");
            demande.setRejectionReason("Service CAV indisponible");
            store.saveDemande(demande);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new DemandeResponse(
                    demande.getId(),
                    demande.getStatus(),
                    null,
                    null,
                    demande.getRejectionReason(),
                    demande.getRequestDate()
                )
            );
        }
        
        // Get RSA calculator actor address
        ActorAddress calculatorAddress = serviceServer.get().address(KnownActors.CALCULATEUR_RSA);
        
        // Create request message
        CalculateRSARequest request = new CalculateRSARequest(
            dto.allocataireId(),
            dto.monthlyIncome(),
            dto.numberOfDependents(),
            dto.inCouple(),
            dto.hasHousing()
        );
        
        log.info("Envoi de CalculateRSARequest vers calculateur RSA");
        
        // Send request and wait for response (synchronous)
        try {
            DecisionAllocationResponse response = world.querySync(calculatorAddress, request);
            
            log.info("Réponse reçue: accepté={}, montant={}€", response.accepted(), response.monthlyAmount());
            
            // Update demande with response
            if (response.accepted()) {
                demande.setStatus("ACCEPTED");
                demande.setAllocationId(response.allocationId());
                demande.setMonthlyAmount(response.monthlyAmount());
            } else {
                demande.setStatus("REJECTED");
                demande.setRejectionReason(response.rejectionReason());
            }
            
            store.saveDemande(demande);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new DemandeResponse(
                    demande.getId(),
                    demande.getStatus(),
                    demande.getAllocationId(),
                    demande.getMonthlyAmount(),
                    demande.getRejectionReason(),
                    demande.getRequestDate()
                )
            );
            
        } catch (Exception e) {
            log.error("Erreur lors de la communication avec le service", e);
            demande.setStatus("REJECTED");
            demande.setRejectionReason("Erreur de communication avec le service");
            store.saveDemande(demande);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new DemandeResponse(
                    demande.getId(),
                    demande.getStatus(),
                    null,
                    null,
                    demande.getRejectionReason(),
                    demande.getRequestDate()
                )
            );
        }
    }
}

