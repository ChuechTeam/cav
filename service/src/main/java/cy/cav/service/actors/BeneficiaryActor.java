package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.protocol.accounts.GetAccountRequest;
import cy.cav.protocol.accounts.GetAccountResponse;
import cy.cav.protocol.allocations.CalculateRSARequest;
import cy.cav.protocol.requests.RequestAllowanceRequest;
import cy.cav.protocol.requests.RequestAllowanceResponse;
import cy.cav.protocol.requests.RequestAllowanceNotification;
import cy.cav.protocol.KnownActors;
import cy.cav.service.store.AllocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Actor representing a single beneficiary (allocataire).
 * 
 * Each beneficiary has its own actor instance with its own data.
 * This follows the pattern "one actor = one entity".
 * 
 * Responsibilities:
 * - Stores beneficiary data
 * - Handles account retrieval requests
 * - Handles allowance request notifications
 */
public class BeneficiaryActor extends Actor {
    private static final Logger log = LoggerFactory.getLogger(BeneficiaryActor.class);
    
    // This actor's beneficiary data
    private final cy.cav.service.domain.Beneficiary beneficiary;
    
    // Store for persistence (UI display)
    private final AllocationStore store;
    
    static final Router<BeneficiaryActor> router = new Router<BeneficiaryActor>()
        .route(GetAccountRequest.class, BeneficiaryActor::getAccount)
        .route(RequestAllowanceRequest.class, BeneficiaryActor::requestAllowance)
        .route(RequestAllowanceNotification.class, BeneficiaryActor::processAllowanceNotification);
    
    public BeneficiaryActor(ActorInit init, cy.cav.service.domain.Beneficiary beneficiary, AllocationStore store) {
        super(init);
        this.beneficiary = beneficiary;
        this.store = store;
    }
    
    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }
    
    /**
     * Gets this beneficiary's account information.
     */
    GetAccountResponse getAccount(GetAccountRequest request) {
        // Verify this is the right beneficiary
        if (!beneficiary.getId().equals(request.beneficiaryId())) {
            log.warn("GetAccountRequest for wrong beneficiary. Expected: {}, got: {}", 
                beneficiary.getId(), request.beneficiaryId());
            throw new RuntimeException("Account not found");
        }
        
        log.debug("Getting account for beneficiary: {}", beneficiary.getId());
        
        // Ensure beneficiary number and registration date are set
        String beneficiaryNumber = beneficiary.getBeneficiaryNumber();
        if (beneficiaryNumber == null) {
            beneficiaryNumber = generateBeneficiaryNumberFor(beneficiary);
            beneficiary.setBeneficiaryNumber(beneficiaryNumber);
        }
        
        LocalDate registrationDate = beneficiary.getRegistrationDate();
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
            beneficiary.setRegistrationDate(registrationDate);
        }
        
        return new GetAccountResponse(
            beneficiary.getId(),
            beneficiaryNumber,
            beneficiary.getFirstName(),
            beneficiary.getLastName(),
            beneficiary.getBirthDate(),
            beneficiary.getEmail(),
            beneficiary.getPhoneNumber(),
            beneficiary.getAddress(),
            beneficiary.isInCouple(),
            beneficiary.getNumberOfDependents(),
            beneficiary.getMonthlyIncome(),
            beneficiary.getIban(),
            registrationDate,
            "ACTIF"
        );
    }
    
    /**
     * Handles allowance request.
     * Creates the request immediately and returns the requestId, then processes asynchronously.
     * 
     * This method:
     * 1. Creates the AllowanceRequest and saves it (synchronous)
     * 2. Returns the requestId immediately (synchronous response)
     * 3. Processes the calculation asynchronously (RSA calculator, etc.)
     */
    RequestAllowanceResponse requestAllowance(RequestAllowanceRequest request) {
        log.info("Processing allowance request for beneficiary: {}, type: {}", 
            beneficiary.getId(), request.allowanceType());
        
        // Verify this is the right beneficiary
        if (!beneficiary.getId().equals(request.beneficiaryId())) {
            log.warn("RequestAllowanceRequest for wrong beneficiary. Expected: {}, got: {}", 
                beneficiary.getId(), request.beneficiaryId());
            throw new RuntimeException("Beneficiary not found");
        }
        
        // Create allowance request immediately (synchronous)
        cy.cav.service.domain.AllowanceRequest allowanceRequest = new cy.cav.service.domain.AllowanceRequest(
            request.beneficiaryId(), request.allowanceType());
        store.saveRequest(allowanceRequest);
        
        log.info("Allowance request created: {} for beneficiary: {}", 
            allowanceRequest.getId(), request.beneficiaryId());
        
        // IMPORTANT: Start asynchronous processing AFTER returning the response
        // Use CompletableFuture to run in background without blocking
        CompletableFuture.runAsync(() -> {
            processAllowanceRequestAsync(
                allowanceRequest,
                request.beneficiaryId(),
                request.monthlyIncome(),
                request.numberOfDependents(),
                request.inCouple(),
                request.hasHousing()
            );
        });
        
        // Return response immediately with requestId
        RequestAllowanceResponse response = new RequestAllowanceResponse(
            allowanceRequest.getId(),
            "PENDING",
            allowanceRequest.getRequestDate()
        );
        
        log.info("Returning RequestAllowanceResponse immediately: requestId={}, status={}", 
            response.requestId(), response.status());
        
        return response;
    }
    
    /**
     * Processes the allowance request asynchronously (RSA calculation, etc.).
     * This runs in the background while the client already has the requestId.
     */
    private void processAllowanceRequestAsync(
            cy.cav.service.domain.AllowanceRequest allowanceRequest,
            UUID beneficiaryId,
            double monthlyIncome,
            int numberOfDependents,
            boolean inCouple,
            boolean hasHousing) {
        
        // Find RSA calculator actor (on same server)
        ActorAddress calculatorAddress = world.server().address(KnownActors.RSA_CALCULATOR);
        
        // Create calculation request
        CalculateRSARequest calcRequest = new CalculateRSARequest(
            beneficiaryId,
            monthlyIncome,
            numberOfDependents,
            inCouple,
            hasHousing
        );
        
        // Send request to calculator (asynchronous)
        world.query(null, calculatorAddress, calcRequest)
            .thenAccept(response -> {
                // Update request with response
                allowanceRequest.setStatus(response.accepted() ? "ACCEPTED" : "REJECTED");
                allowanceRequest.setAllowanceId(response.allocationId());
                allowanceRequest.setMonthlyAmount(response.monthlyAmount());
                allowanceRequest.setRejectionReason(response.rejectionReason());
                store.saveRequest(allowanceRequest);
                
                log.info("Allowance request {} processed: status={}, amount={}", 
                    allowanceRequest.getId(), allowanceRequest.getStatus(), allowanceRequest.getMonthlyAmount());
            })
            .exceptionally(throwable -> {
                log.error("Error processing allowance request", throwable);
                allowanceRequest.setStatus("REJECTED");
                allowanceRequest.setRejectionReason("Error processing request: " + throwable.getMessage());
                store.saveRequest(allowanceRequest);
                return null;
            });
    }
    
    /**
     * Processes allowance request notification (asynchronous).
     * This is called by Prefecture after it has created the request and returned the response.
     * The requestId is already known, so we just need to process it.
     */
    void processAllowanceNotification(Envelope<RequestAllowanceNotification> envelope) {
        RequestAllowanceNotification notification = envelope.body();
        log.info("Processing allowance notification for beneficiary: {}, type: {}", 
            beneficiary.getId(), notification.allowanceType());
        
        // Verify this is the right beneficiary
        if (!beneficiary.getId().equals(notification.beneficiaryId())) {
            log.warn("RequestAllowanceNotification for wrong beneficiary. Expected: {}, got: {}", 
                beneficiary.getId(), notification.beneficiaryId());
            return;
        }
        
        // Find the allowance request in the store using the requestId from notification
        Optional<cy.cav.service.domain.AllowanceRequest> requestOpt = 
            store.findRequestById(notification.requestId());
        
        if (requestOpt.isEmpty()) {
            log.warn("Allowance request not found: {} for beneficiary: {}", 
                notification.requestId(), beneficiary.getId());
            return;
        }
        
        cy.cav.service.domain.AllowanceRequest allowanceRequest = requestOpt.get();
        
        // Verify the request belongs to this beneficiary
        if (!allowanceRequest.getBeneficiaryId().equals(beneficiary.getId())) {
            log.warn("Allowance request {} does not belong to beneficiary: {}", 
                notification.requestId(), beneficiary.getId());
            return;
        }
        
        log.info("Found allowance request: {} for beneficiary: {}", 
            allowanceRequest.getId(), beneficiary.getId());
        
        // Process the request asynchronously
        processAllowanceRequestAsync(
            allowanceRequest,
            notification.beneficiaryId(),
            notification.monthlyIncome(),
            notification.numberOfDependents(),
            notification.inCouple(),
            notification.hasHousing()
        );
    }
    
    /**
     * Generates beneficiary number for an existing beneficiary (deterministic).
     */
    private String generateBeneficiaryNumberFor(cy.cav.service.domain.Beneficiary beneficiary) {
        // For existing beneficiaries, use a deterministic approach based on ID
        String idStr = beneficiary.getId().toString().replace("-", "");
        return "CAV" + idStr.substring(0, Math.min(11, idStr.length()));
    }
}

