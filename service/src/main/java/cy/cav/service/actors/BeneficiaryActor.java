package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;
import cy.cav.protocol.*;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.allowances.*;
import cy.cav.protocol.requests.*;
import cy.cav.service.*;
import cy.cav.service.domain.*;
import cy.cav.service.domain.AllowancePrevision;
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
    private final Store store;
    private final ServerFinder serverFinder;

    /// Allowances that are wanted by the beneficiary, with their previsions.
    ///
    /// Each type is guaranteed to have a prevision in this map.
    private final Map<AllowanceType, AllowancePrevision> allowancePrevisions = new HashMap<>();

    /// Sends messages over and over, to calculators.
    private final AckRetryer retryer = AckRetryer.additiveDelay(this, Duration.ofSeconds(15), Duration.ofSeconds(10));

    private final AckStore<ReceivePayments.Ack> paymentAckStore = new AckStore<>(this);

    private LocalDate currentMonth;

    static final Router<BeneficiaryActor> router = new Router<BeneficiaryActor>()
            .route(GetAccountRequest.class, BeneficiaryActor::getAccount)
            .route(RequestAllowanceRequest.class, BeneficiaryActor::requestAllowance)
            .route(CalculateAllowance.Ack.class, BeneficiaryActor::allowanceCalculated)
            .route(PayAllowances.class, BeneficiaryActor::payAllowances)
            .route(ReceivePayments.class, BeneficiaryActor::receivePayments);

    public BeneficiaryActor(ActorInit init, LocalDate currentMonth, Beneficiary beneficiary, Store store, ServerFinder serverFinder) {
        super(init);
        // Initialize all prevision types with default previsions.
        for (AllowanceType type : AllowanceType.values()) {
            allowancePrevisions.put(type, new AllowancePrevision(type));
        }
        this.beneficiary = beneficiary;
        this.store = store;
        this.currentMonth = currentMonth;
        this.serverFinder = serverFinder;
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

        store.beneficiaries().put(beneficiary.getId(), beneficiary);
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
                List.copyOf(beneficiary.getPayments()),
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
        CalculateAllowance message = new CalculateAllowance(beneficiary.toProfile());
        retryer.send(_ -> serverFinder.pickCalculatorActor(request.type()), message);

        // Update the prevision --> PENDING
        AllowancePrevision prevision = allowancePrevisions.get(request.type());
        if (prevision.getAckId() != null) {
            retryer.giveUp(prevision.getAckId());
        }
        prevision.start(message.ackId());

        return new RequestAllowanceResponse(true, "La demande a bien été envoyée !");
    }

    private void allowanceCalculated(CalculateAllowance.Ack ack) {
        log.info("Received allowance calculation: {}", ack);

        AllowancePrevision prevision = allowancePrevisions.get(ack.type());
        prevision.receiveResult(ack.ackId(), ack.amount(), ack.message());
    }

    private void payAllowances(Envelope<PayAllowances> envelope) {
        // First see if we already paid this month of allowances.
        PayAllowances message = envelope.body();
        if (currentMonth.isAfter(message.month())) {
            log.info("Received PayAllowance with a month in the past: {}; ignoring", message.month());
            send(envelope.sender(), new PayAllowances.Ack(message.ackId()));
            return;
        }

        // Gather all allowance types we want
        Set<AllowanceType> wantedTypes = EnumSet.noneOf(AllowanceType.class);
        for (AllowancePrevision prevision : allowancePrevisions.values()) {
            if (prevision.getState() != AllowancePrevisionState.UNWANTED) {
                wantedTypes.add(prevision.getType());
            }
        }

        // Then start a payment process if we want some allowances
        if (!wantedTypes.isEmpty()) {
            BeneficiaryProfile profile = beneficiary.toProfile();
            world.spawn(init -> new PaymentProcess(init, serverFinder, address, profile, message.month(), wantedTypes));
        }

        // Switch to the next month
        LocalDate prevMonth = currentMonth;
        currentMonth = message.month().plusMonths(1);
        log.info("Beneficiary {} has now moved from month {} to {}", address, prevMonth, currentMonth);

        // All good!
        send(envelope.sender(), new PayAllowances.Ack(message.ackId()));
    }

    private void receivePayments(Envelope<ReceivePayments> envelope) {
        if (paymentAckStore.sendIfAcknowledged(envelope)) {
            return;
        }

        ReceivePayments message = envelope.body();
        for (cy.cav.protocol.Payment payment : message.payments()) {
            beneficiary.getPayments().add(new Payment(payment.label(), payment.amount()));
        }

        log.info("Received {} payments from actor {}", message.payments().size(), envelope.sender());
        paymentAckStore.send(envelope.sender(), new ReceivePayments.Ack(message.ackId()));
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

