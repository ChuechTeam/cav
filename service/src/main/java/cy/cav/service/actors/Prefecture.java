package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.requests.RequestAllowanceRequest;
import cy.cav.protocol.requests.RequestAllowanceResponse;
import cy.cav.protocol.requests.RequestAllowanceNotification;
import cy.cav.service.store.AllocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prefecture actor that manages beneficiary actors.
 * 
 * Responsibilities:
 * - Creates new beneficiary actors (spawns BeneficiaryActor)
 * - Maintains registry of beneficiary actors (UUID → ActorAddress)
 * - Routes messages to the correct beneficiary actors
 * - Checks if beneficiaries exist
 * 
 * This replaces the old Beneficiary actor which managed all beneficiaries in a Map.
 * Now each beneficiary has its own actor (BeneficiaryActor).
 */
public class Prefecture extends Actor {
    private static final Logger log = LoggerFactory.getLogger(Prefecture.class);
    
    // Registry: maps beneficiary UUID to their actor address
    private final Map<UUID, ActorAddress> beneficiaryActors = new ConcurrentHashMap<>();
    
    // Store for persistence (UI display and fallback)
    private final AllocationStore store;
    
    static final Router<Prefecture> router = new Router<Prefecture>()
        .route(CreateAccountRequest.class, Prefecture::createAccount)
        .route(GetAccountRequest.class, Prefecture::getAccount)
        .route(CheckAccountExistsRequest.class, Prefecture::checkAccountExists)
        .route(RequestAllowanceRequest.class, Prefecture::routeAllowanceRequest);
    
    public Prefecture(ActorInit init, AllocationStore store) {
        super(init);
        this.store = store;
    }
    
    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }
    
    /**
     * Creates a new beneficiary account by spawning a BeneficiaryActor.
     */
    CreateAccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for: {} {}", request.firstName(), request.lastName());
        
        // Create beneficiary domain object
        cy.cav.service.domain.Beneficiary beneficiary = new cy.cav.service.domain.Beneficiary(
            request.firstName(),
            request.lastName(),
            request.birthDate(),
            request.email(),
            request.inCouple(),
            request.numberOfDependents()
        );
        
        beneficiary.setPhoneNumber(request.phoneNumber());
        beneficiary.setAddress(request.address());
        beneficiary.setMonthlyIncome(request.monthlyIncome());
        beneficiary.setIban(request.iban());
        
        // Generate beneficiary number and registration date
        LocalDate registrationDate = LocalDate.now();
        String beneficiaryNumber = generateBeneficiaryNumber();
        beneficiary.setBeneficiaryNumber(beneficiaryNumber);
        beneficiary.setRegistrationDate(registrationDate);
        
        // Spawn a new BeneficiaryActor for this beneficiary
        ActorAddress actorAddress = world.spawn(init -> new BeneficiaryActor(init, beneficiary, store));
        beneficiaryActors.put(beneficiary.getId(), actorAddress);
        
        log.info("BeneficiaryActor spawned for {} (ID: {}, Actor: {})", 
            beneficiaryNumber, beneficiary.getId(), actorAddress);
        
        // Also save to store for UI display
        store.saveBeneficiary(beneficiary);
        
        log.info("Account created: {} (ID: {})", beneficiaryNumber, beneficiary.getId());
        
        return new CreateAccountResponse(
            beneficiary.getId(),
            beneficiaryNumber,
            registrationDate,
            "ACTIVE"
        );
    }
    
    /**
     * Gets a beneficiary account by routing to the corresponding BeneficiaryActor.
     * If the actor doesn't exist, tries to load from store and spawns a new actor.
     */
    GetAccountResponse getAccount(GetAccountRequest request) {
        log.debug("Getting account: {}", request.beneficiaryId());
        
        ActorAddress actorAddress = beneficiaryActors.get(request.beneficiaryId());
        
        if (actorAddress != null) {
            // Actor exists, route the request to it
            log.debug("Routing GetAccountRequest to BeneficiaryActor: {}", actorAddress);
            return world.querySync(actorAddress, request);
        }
        
        // Actor doesn't exist, try to load from store (e.g., after restart)
        Optional<cy.cav.service.domain.Beneficiary> beneficiaryOpt = 
            store.findBeneficiaryById(request.beneficiaryId());
        
        if (beneficiaryOpt.isPresent()) {
            cy.cav.service.domain.Beneficiary beneficiary = beneficiaryOpt.get();
            log.info("Beneficiary found in store, spawning new BeneficiaryActor: {}", request.beneficiaryId());
            
            // Spawn a new actor for this existing beneficiary
            actorAddress = world.spawn(init -> new BeneficiaryActor(init, beneficiary, store));
            beneficiaryActors.put(beneficiary.getId(), actorAddress);
            
            // Route the request to the newly spawned actor
            return world.querySync(actorAddress, request);
        }
        
        // Not found
        log.warn("Account not found: {}", request.beneficiaryId());
        throw new RuntimeException("Account not found");
    }
    
    /**
     * Checks if a beneficiary account exists.
     */
    CheckAccountExistsResponse checkAccountExists(CheckAccountExistsRequest request) {
        boolean exists = beneficiaryActors.containsKey(request.beneficiaryId()) || 
                        store.findBeneficiaryById(request.beneficiaryId()).isPresent();
        log.debug("Checking account existence {}: {}", request.beneficiaryId(), exists);
        return new CheckAccountExistsResponse(exists);
    }
    
    /**
     * Routes allowance request to the corresponding BeneficiaryActor and returns the response IMMEDIATELY.
     * 
     * Strategy:
     * 1. Create the AllowanceRequest immediately (to get requestId)
     * 2. Return response with requestId and PENDING status (synchronous)
     * 3. Send notification to BeneficiaryActor to process asynchronously (fire and forget)
     */
    RequestAllowanceResponse routeAllowanceRequest(Envelope<RequestAllowanceRequest> envelope) {
        RequestAllowanceRequest request = envelope.body();
        UUID beneficiaryId = request.beneficiaryId();
        
        log.info("Routing allowance request for beneficiary: {}", beneficiaryId);
        
        // STEP 1: Create the allowance request immediately to get the requestId
        cy.cav.service.domain.AllowanceRequest allowanceRequest = new cy.cav.service.domain.AllowanceRequest(
            beneficiaryId, request.allowanceType());
        store.saveRequest(allowanceRequest);
        
        log.info("Allowance request created: {} for beneficiary: {}", 
            allowanceRequest.getId(), beneficiaryId);
        
        // STEP 2: Find or spawn BeneficiaryActor
        ActorAddress actorAddress = beneficiaryActors.get(beneficiaryId);
        
        if (actorAddress == null) {
            // Try to load from store and spawn actor
            Optional<cy.cav.service.domain.Beneficiary> beneficiaryOpt = 
                store.findBeneficiaryById(beneficiaryId);
            
            if (beneficiaryOpt.isPresent()) {
                cy.cav.service.domain.Beneficiary beneficiary = beneficiaryOpt.get();
                log.info("Beneficiary found in store, spawning new BeneficiaryActor: {}", beneficiaryId);
                
                actorAddress = world.spawn(init -> new BeneficiaryActor(init, beneficiary, store));
                beneficiaryActors.put(beneficiary.getId(), actorAddress);
            } else {
                log.warn("Beneficiary not found: {}", beneficiaryId);
                throw new RuntimeException("Beneficiary not found: " + beneficiaryId);
            }
        }
        
        // STEP 3: Send notification to BeneficiaryActor to process asynchronously (fire and forget)
        // We use RequestAllowanceNotification which BeneficiaryActor can handle
        RequestAllowanceNotification notification = new RequestAllowanceNotification(
            allowanceRequest.getId(),  // requestId - ID of the allowance request
            beneficiaryId,
            request.allowanceType(),
            request.monthlyIncome(),
            request.numberOfDependents(),
            request.inCouple(),
            request.hasHousing()
        );
        
        log.debug("Sending RequestAllowanceNotification to BeneficiaryActor: {} (async processing)", actorAddress);
        world.send(address(), actorAddress, notification);
        
        // STEP 4: Return response IMMEDIATELY with requestId (don't wait for processing)
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
     * Generates a unique beneficiary number.
     */
    private String generateBeneficiaryNumber() {
        LocalDate now = LocalDate.now();
        return "CAV" + now.getYear() + 
               String.format("%02d", now.getMonthValue()) +
               String.format("%02d", now.getDayOfMonth()) +
               String.format("%03d", (int)(Math.random() * 1000));
    }
}

