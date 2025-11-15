package cy.cav.client.controller;

import cy.cav.client.dto.DemandeRSADTO;
import cy.cav.client.dto.DemandeResponse;
import cy.cav.framework.*;
import cy.cav.protocol.KnownActors;
import cy.cav.protocol.accounts.CheckAccountExistsRequest;
import cy.cav.protocol.accounts.CheckAccountExistsResponse;
import cy.cav.protocol.allocations.CalculateRSARequest;
import cy.cav.protocol.allocations.DecisionAllocationResponse;
import cy.cav.protocol.demandes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// REST controller for allocation requests (gestion des demandes d'allocation)
@RestController
@RequestMapping("/api/demandes")
public class AllocRequestController {
    private static final Logger log = LoggerFactory.getLogger(AllocRequestController.class);
    
    private final World world;
    private final Network network;
    
    public AllocRequestController(World world, Network network) {
        this.world = world;
        this.network = network;
    }
    
    // Creates RSA allocation request (création d'une demande RSA)
    @PostMapping("/rsa")
    public ResponseEntity<DemandeResponse> demanderRSA(@RequestBody DemandeRSADTO dto) {
        log.info("Demande RSA reçue pour allocataire: {}", dto.allocataireId());
        
        // Check if allocataire exists via proxy
        try {
            CheckAccountExistsRequest checkRequest = new CheckAccountExistsRequest(dto.allocataireId());
            ActorAddress proxyAddress = world.server().address(KnownActors.GESTIONNAIRE_COMPTE);
            CheckAccountExistsResponse checkResponse = world.querySync(proxyAddress, checkRequest);
            
            if (!checkResponse.exists()) {
                log.warn("Allocataire non trouvé: {}", dto.allocataireId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de l'allocataire", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        // Create demande via proxy
        UUID demandeId;
        try {
            CreateDemandeRequest createRequest = new CreateDemandeRequest(dto.allocataireId(), "RSA");
            ActorAddress proxyAddress = world.server().address(KnownActors.GESTIONNAIRE_COMPTE);
            CreateDemandeResponse createResponse = world.querySync(proxyAddress, createRequest);
            demandeId = createResponse.demandeId();
            log.info("Demande créée: {} pour allocataire: {}", demandeId, dto.allocataireId());
        } catch (Exception e) {
            log.error("Erreur lors de la création de la demande", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
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
            // Update demande via proxy
            try {
                UpdateDemandeRequest updateRequest = new UpdateDemandeRequest(
                    demandeId, "REJECTED", null, null, "Service CAV indisponible"
                );
                ActorAddress proxyAddress = world.server().address(KnownActors.GESTIONNAIRE_COMPTE);
                world.querySync(proxyAddress, updateRequest);
            } catch (Exception e) {
                log.error("Erreur lors de la mise à jour de la demande", e);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new DemandeResponse(
                    demandeId,
                    "REJECTED",
                    null,
                    null,
                    "Service CAV indisponible",
                    java.time.LocalDate.now()
                ));
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
            
            // Update demande with response via proxy
            try {
                UpdateDemandeRequest updateRequest = new UpdateDemandeRequest(
                    demandeId,
                    response.accepted() ? "ACCEPTED" : "REJECTED",
                    response.allocationId(),
                    response.monthlyAmount(),
                    response.rejectionReason()
                );
                ActorAddress proxyAddress = world.server().address(KnownActors.GESTIONNAIRE_COMPTE);
                world.querySync(proxyAddress, updateRequest);
            } catch (Exception e) {
                log.error("Erreur lors de la mise à jour de la demande", e);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DemandeResponse(
                    demandeId,
                    response.accepted() ? "ACCEPTED" : "REJECTED",
                    response.allocationId(),
                    response.monthlyAmount(),
                    response.rejectionReason(),
                    java.time.LocalDate.now()
                ));
            
        } catch (Exception e) {
            log.error("Erreur lors de la communication avec le service", e);
            // Update demande via proxy
            try {
                UpdateDemandeRequest updateRequest = new UpdateDemandeRequest(
                    demandeId, "REJECTED", null, null, "Erreur de communication avec le service"
                );
                ActorAddress proxyAddress = world.server().address(KnownActors.GESTIONNAIRE_COMPTE);
                world.querySync(proxyAddress, updateRequest);
            } catch (Exception ex) {
                log.error("Erreur lors de la mise à jour de la demande", ex);
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DemandeResponse(
                    demandeId,
                    "REJECTED",
                    null,
                    null,
                    "Erreur de communication avec le service",
                    java.time.LocalDate.now()
                ));
        }
    }
}

