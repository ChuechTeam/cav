package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;
import cy.cav.protocol.*;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.allowances.*;
import cy.cav.protocol.requests.*;
import cy.cav.service.domain.*;
import cy.cav.service.domain.AllowancePrevision;
import cy.cav.service.store.*;
import org.slf4j.*;

import java.time.*;
import java.util.*;

/**
 * Actor representing a single beneficiary (allocataire).
 * <p>
 * Each beneficiary has its own actor instance with its own data.
 * This follows the pattern "one actor = one entity".
 * <p>
 * Responsibilities:
 * - Stores beneficiary data
 * - Handles account retrieval requests
 * - Handles allowance request notifications
 */
public class BeneficiaryActor extends Actor {
    private static final Logger log = LoggerFactory.getLogger(BeneficiaryActor.class);

    // This actor's beneficiary data
    private final Beneficiary beneficiary;

    // Store for persistence (UI display)
    private final AllocationStore store;

    /// Allowances that are wanted by the beneficiary, with their previsions.
    ///
    /// Each type is guaranteed to have a prevision in this map.
    private final Map<AllowanceType, AllowancePrevision> allowancePrevisions = new HashMap<>();

    /// Sends messages over and over, to calculators.
    private final AckRetryer retryer = AckRetryer.additiveDelay(this, Duration.ofSeconds(15), Duration.ofSeconds(10));

    static final Router<BeneficiaryActor> router = new Router<BeneficiaryActor>()
            .route(GetAccountRequest.class, BeneficiaryActor::getAccount)
            .route(RequestAllowanceRequest.class, BeneficiaryActor::requestAllowance)
            .route(AllowanceCalculated.class, BeneficiaryActor::allowanceCalculated);

    public BeneficiaryActor(ActorInit init, Beneficiary beneficiary, AllocationStore store) {
        super(init);
        // Initialize all prevision types with default previsions.
        for (AllowanceType type : AllowanceType.values()) {
            allowancePrevisions.put(type, new AllowancePrevision(type));
        }
        this.beneficiary = beneficiary;
        this.store = store;
    }

    @Override
    protected void spawned() {
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

        store.saveBeneficiary(beneficiary);
    }

    @Override
    protected void process(Envelope<?> envelope) {
        if (retryer.process(envelope)) {
            return;
        }

        router.process(this, envelope);
    }

    /**
     * Gets this beneficiary's account information.
     */
    GetAccountResponse getAccount(GetAccountRequest request) {
        log.debug("Getting account for beneficiary: {}", beneficiary.getId());

        // Convert our allowance prevision map to protocol format.
        Map<AllowanceType, cy.cav.protocol.AllowancePrevision> protocolPrevisions = new HashMap<>();
        for (Map.Entry<AllowanceType, AllowancePrevision> entry : allowancePrevisions.entrySet()) {
            protocolPrevisions.put(entry.getKey(), entry.getValue().toProtocol());
        }

        return new GetAccountResponse(
                beneficiary.getId(),
                beneficiary.toProfile(),
                protocolPrevisions
        );
    }

    /**
     * Handles allowance request.
     * Creates the request, then processes asynchronously.
     */
    RequestAllowanceResponse requestAllowance(RequestAllowanceRequest request) {
        log.info("Processing allowance request for beneficiary: {}; {}",
                beneficiary.getId(), request);

        // Create the message and send it to the calculator
        ActorAddress calculator = request.type().calculatorActor(world.server());
        CalculateAllowance message = new CalculateAllowance(beneficiary.toProfile());
        retryer.send(calculator, message);

        // Update the prevision --> PENDING
        AllowancePrevision prevision = allowancePrevisions.get(request.type());
        prevision.start(message.ackId());

        return new RequestAllowanceResponse(true, "La demande a bien été envoyée !");
    }

    private void allowanceCalculated(AllowanceCalculated allowanceCalculated) {
        log.info("Received allowance calculation: {}", allowanceCalculated);

        AllowancePrevision prevision = allowancePrevisions.get(allowanceCalculated.type());
        prevision.receiveResult(allowanceCalculated.ackId(), allowanceCalculated.amount(), allowanceCalculated.message());
    }

    /**
     * Generates beneficiary number for an existing beneficiary (deterministic).
     */
    private String generateBeneficiaryNumberFor(Beneficiary beneficiary) {
        // For existing beneficiaries, use a deterministic approach based on ID
        String idStr = beneficiary.getId().toString().replace("-", "");
        return "CAV" + idStr.substring(0, Math.min(11, idStr.length()));
    }
}

