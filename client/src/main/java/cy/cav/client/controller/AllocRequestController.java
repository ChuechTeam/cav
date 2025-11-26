package cy.cav.client.controller;

import cy.cav.client.ServiceAPI;
import cy.cav.client.dto.AllowanceRequestDTO;
import cy.cav.client.dto.AllowanceRequestResponse;
import cy.cav.framework.*;
import cy.cav.protocol.KnownActors;
import cy.cav.protocol.accounts.CheckAccountExistsRequest;
import cy.cav.protocol.accounts.CheckAccountExistsResponse;
import cy.cav.protocol.requests.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// REST controller for allowance requests
@RestController
@RequestMapping("/api/requests")
public class AllocRequestController {
    private static final Logger log = LoggerFactory.getLogger(AllocRequestController.class);
    
    private final ServiceAPI serviceAPI;
    private final World world;
    private final Network network;
    
    public AllocRequestController(ServiceAPI serviceAPI, World world, Network network) {
        this.serviceAPI = serviceAPI;
        this.world = world;
        this.network = network;
    }
    
    // Creates RSA allowance request
    @PostMapping("/rsa")
    public ResponseEntity<AllowanceRequestResponse> requestRSA(@RequestBody AllowanceRequestDTO dto) {
        log.info("RSA request received for beneficiary: {}", dto.beneficiaryId());
        
        // Check if beneficiary exists via ServiceAPI
        try {
            CheckAccountExistsRequest checkRequest = new CheckAccountExistsRequest(dto.beneficiaryId());
            CheckAccountExistsResponse checkResponse = serviceAPI.checkAccountExists(checkRequest);
            
            if (!checkResponse.exists()) {
                log.warn("Beneficiary not found: {}", dto.beneficiaryId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("Error checking beneficiary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        // Find service via Network (Eureka)
        var serviceServer = network.servers().values().stream()
            .filter(server -> server.appName().equalsIgnoreCase("cav-service"))
            .findFirst();
        
        if (serviceServer.isEmpty()) {
            log.error("Service cav-service not found via Eureka");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new AllowanceRequestResponse(
                    null,
                    "REJECTED",
                    null,
                    null,
                    "Service CAV unavailable",
                    java.time.LocalDate.now()
                ));
        }
        
        // Get Beneficiary actor address
        ActorAddress beneficiaryAddress = serviceServer.get().address(KnownActors.BENEFICIARY);
        
        // Send notification to Beneficiary actor (triggers full process on server side)
        RequestAllowanceNotification notification = new RequestAllowanceNotification(
            dto.beneficiaryId(),
            "RSA",
            dto.monthlyIncome(),
            dto.numberOfDependents(),
            dto.inCouple(),
            dto.hasHousing()
        );
        
        log.info("Sending RequestAllowanceNotification to Beneficiary actor");
        
        // Send notification (fire and forget)
        world.send(world.server().address(), beneficiaryAddress, notification);
        
        // TODO: requestId inconnu côté client, important pour l'endpoint GET /api/requests/{requestId}
        // 
        // PROBLÈME :
        // - Demande créée de manière asynchrone côté server (dans Beneficiary.requestAllowance)
        // - Le client peut pas connaître le requestId tout de suite (car statut PENDING)
        // - Le client ne peut donc pas suivre la demande via endpoint GET /api/requests/{requestId}
        // à voir
        
        // Return immediate response (request is being processed asynchronously)
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(new AllowanceRequestResponse(
                null,  // request id va être généré sur le serveur
                "PENDING",
                null,
                null,
                null,
                java.time.LocalDate.now()
            ));
    }
    
    // Gets allowance request by ID
    @GetMapping("/{requestId}")
    public ResponseEntity<AllowanceRequestResponse> getAllowanceRequest(@PathVariable UUID requestId) {
        log.info("Getting allowance request: {}", requestId);
        
        try {
            GetAllowanceRequestRequest request = new GetAllowanceRequestRequest(requestId);
            GetAllowanceRequestResponse response = serviceAPI.getAllowanceRequest(request);
            
            return ResponseEntity.ok(
                new AllowanceRequestResponse(
                    response.requestId(),
                    response.status(),
                    response.allowanceId(),
                    response.monthlyAmount(),
                    response.rejectionReason(),
                    response.requestDate()
                )
            );
        } catch (Exception e) {
            log.error("Error getting allowance request: {}", requestId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
