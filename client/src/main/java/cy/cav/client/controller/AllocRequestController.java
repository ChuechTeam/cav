package cy.cav.client.controller;

import cy.cav.client.ServiceAPI;
import cy.cav.client.dto.AllowanceRequestDTO;
import cy.cav.client.dto.AllowanceRequestResponse;
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
    
    public AllocRequestController(ServiceAPI serviceAPI) {
        this.serviceAPI = serviceAPI;
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
        
        // Send request via ServiceAPI (Request/Response pattern)
        try {
            RequestAllowanceRequest request = new RequestAllowanceRequest(
                dto.beneficiaryId(),
                "RSA",
                dto.monthlyIncome(),
                dto.numberOfDependents(),
                dto.inCouple(),
                dto.hasHousing()
            );
            
            log.info("Sending RequestAllowanceRequest to Prefecture");
            
            // Get response with requestId immediately
            RequestAllowanceResponse response = serviceAPI.requestAllowance(request);
            
            log.info("Allowance request created: {} for beneficiary: {}", 
                response.requestId(), dto.beneficiaryId());
            
            // Return response with requestId (processing continues asynchronously)
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new AllowanceRequestResponse(
                    response.requestId(),  // Now we have the requestId!
                    response.status(),
                    null,  // allowanceId will be set when processing completes
                    null,  // monthlyAmount will be set when processing completes
                    null,  // rejectionReason will be set if rejected
                    response.requestDate()
                ));
        } catch (Exception e) {
            log.error("Error creating allowance request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AllowanceRequestResponse(
                    null,
                    "REJECTED",
                    null,
                    null,
                    "Error creating request: " + e.getMessage(),
                    java.time.LocalDate.now()
                ));
        }
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
