package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.allocations.CalculateRSARequest;
import cy.cav.protocol.requests.*;
import cy.cav.protocol.KnownActors;
import cy.cav.service.store.AllocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cy.cav.service.domain.AllowanceRequest;

/**
 * Actor that manages beneficiary accounts.
 * Maintains internal state (beneficiaries in memory) and handles allowance requests.
 */
public class Beneficiary extends Actor {
    private static final Logger log = LoggerFactory.getLogger(Beneficiary.class);

    // Internal state: beneficiaries managed by this actor
    private final Map<UUID, cy.cav.service.domain.Beneficiary> beneficiaries = new ConcurrentHashMap<>();

    // Store for persistence (UI display)
    private final AllocationStore store;

    static final Router<Beneficiary> router = new Router<Beneficiary>()
        .route(CreateAccountRequest.class, Beneficiary::createAccount)
        .route(GetAccountRequest.class, Beneficiary::getAccount)
        .route(CheckAccountExistsRequest.class, Beneficiary::checkAccountExists)
        .route(RequestAllowanceNotification.class, Beneficiary::requestAllowance);

    public Beneficiary(ActorInit init, AllocationStore store) {
        super(init);
        this.store = store;
    }

    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }

    
    /**
     * Creates a new beneficiary account.
     */
    CreateAccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for: {} {}", request.firstName(), request.lastName());

        // Create beneficiary
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

        // Save to internal state
        beneficiaries.put(beneficiary.getId(), beneficiary);

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
     * Gets a beneficiary account by ID.
     */
    GetAccountResponse getAccount(GetAccountRequest request) {
        log.debug("Getting account: {}", request.beneficiaryId());

        cy.cav.service.domain.Beneficiary beneficiary = beneficiaries.get(request.beneficiaryId());
        if (beneficiary == null) {
            // Fallback to store if not in internal state
            beneficiary = store.findBeneficiaryById(request.beneficiaryId()).orElse(null);
        }
        
        if (beneficiary == null) {
            log.warn("Account not found: {}", request.beneficiaryId());
            throw new RuntimeException("Account not found");
        }

        return Optional.of(beneficiary)
            .map(b -> {
                // Generate number if not set (for backward compatibility)
                String beneficiaryNumber = b.getBeneficiaryNumber();
                if (beneficiaryNumber == null) {
                    beneficiaryNumber = generateBeneficiaryNumberFor(b);
                    b.setBeneficiaryNumber(beneficiaryNumber);
                }

                LocalDate registrationDate = b.getRegistrationDate();
                if (registrationDate == null) {
                    registrationDate = LocalDate.now();
                    b.setRegistrationDate(registrationDate);
                }

                return new GetAccountResponse(
                    b.getId(),
                    beneficiaryNumber,
                    b.getFirstName(),
                    b.getLastName(),
                    b.getBirthDate(),
                    b.getEmail(),
                    b.getPhoneNumber(),
                    b.getAddress(),
                    b.isInCouple(),
                    b.getNumberOfDependents(),
                    b.getMonthlyIncome(),
                    b.getIban(),
                    registrationDate,
                    "ACTIF"
                );
            })
            .orElseThrow(() -> {
                log.warn("Account not found: {}", request.beneficiaryId());
                return new RuntimeException("Account not found");
            });
    }

    /**
     * Checks if a beneficiary account exists.
     */
    CheckAccountExistsResponse checkAccountExists(CheckAccountExistsRequest request) {
        boolean exists = beneficiaries.containsKey(request.beneficiaryId()) ||
                store.findBeneficiaryById(request.beneficiaryId()).isPresent();
        log.debug("Checking account existence {}: {}", request.beneficiaryId(), exists);
        return new CheckAccountExistsResponse(exists);
    }

    /**
     * Handles allowance request notification.
     * This method orchestrates the full allowance request process on the server side.
     * 
     * TODO: Problème de conception - requestId créé de manière asynchrone
     * 
     * PROBLÈME ACTUEL:
     * - La demande est créée ici de manière asynchrone (ligne 180-182)
     * - Le requestId est généré mais le client ne le connaît pas
     * - Le client ne peut pas suivre la demande via GET /api/requests/{requestId}
     * 
     * SOLUTIONS POSSIBLES À DISCUTER:
     * 1. Créer la demande d'abord côté client (synchrone) via ServiceAPI.createAllowanceRequest(),
     *    puis envoyer la notification avec le requestId existant
     * 2. Modifier RequestAllowanceNotification pour inclure un requestId optionnel
     * 3. Ajouter endpoint GET /api/requests?beneficiaryId={id} pour lister les demandes
     * 4. Pattern Request-Response : créer la demande synchrone, traitement asynchrone en arrière-plan
     * 
     * Voir commentaire TODO dans AllocRequestController pour plus de détails.
     * 
     * Solution implémentée : option 1
     * Demande synchrone coté client via CreateAllowanceRequestRequest puis gestion asynchrone ici.
     * 
     */
    void requestAllowance(Envelope<RequestAllowanceNotification> envelope) {
        RequestAllowanceNotification notification = envelope.body();
        log.info("Processing allowance request {} for beneficiary: {}, type: {}",
            notification.requestId(), notification.beneficiaryId(), notification.allowanceType());

        cy.cav.service.domain.Beneficiary beneficiary = beneficiaries.get(notification.beneficiaryId());
        if (beneficiary == null) {
            log.warn("Beneficiary not in internal state: {}", notification.beneficiaryId());
            beneficiary = store.findBeneficiaryById(notification.beneficiaryId()).orElse(null);

            if (beneficiary == null) {
                log.error("Beneficiary {} not found", notification.beneficiaryId());
                return;
            }

            beneficiaries.put(beneficiary.getId(), beneficiary);
        }

        // Retrieve existing request created synchronously by the client
        Optional<AllowanceRequest> opt = store.findRequestById(notification.requestId());
        if (opt.isEmpty()) {
            log.error("Request {} not found while processing allowance", notification.requestId());
            return;
        }

        AllowanceRequest request = opt.get();



        ActorAddress calculatorAddress = world.server().address(KnownActors.RSA_CALCULATOR);

        CalculateRSARequest calcRequest = new CalculateRSARequest(
            notification.beneficiaryId(),
            notification.monthlyIncome(),
            notification.numberOfDependents(),
            notification.inCouple(),
            notification.hasHousing()
        );

        world.query(null, calculatorAddress, calcRequest)
            .thenAccept(response -> {
                UpdateAllowanceRequestRequest updateRequest = new UpdateAllowanceRequestRequest(
                    request.getId(),
                    response.accepted() ? "ACCEPTED" : "REJECTED",
                    response.allocationId(),
                    response.monthlyAmount(),
                    response.rejectionReason()
                );

                ActorAddress requestActor = world.server().address(KnownActors.ALLOWANCE_REQUEST);

                world.send(world.server().address(), requestActor, updateRequest);

                log.info("Allowance request {} processed asynchronously", request.getId());
            })
            .exceptionally(throwable -> {
                UpdateAllowanceRequestRequest updateRequest = new UpdateAllowanceRequestRequest(
                    request.getId(),
                    "REJECTED",
                    null,
                    null,
                    "Error processing request: " + throwable.getMessage()
                );

                ActorAddress requestActor = world.server().address(KnownActors.ALLOWANCE_REQUEST);
                world.send(world.server().address(), requestActor, updateRequest);

                log.error("Error processing allowance request {}", request.getId(), throwable);
                return null;
            });

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

    /**
     * Generates beneficiary number for an existing beneficiary (deterministic).
     */
    private String generateBeneficiaryNumberFor(cy.cav.service.domain.Beneficiary beneficiary) {
        // For existing beneficiaries, use a deterministic approach based on ID
        String idStr = beneficiary.getId().toString().replace("-", "");
        return "CAV" + idStr.substring(0, Math.min(11, idStr.length()));
    }
}
