package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;
import cy.cav.protocol.*;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.allowances.*;
import cy.cav.protocol.requests.*;
import cy.cav.service.*;
import cy.cav.service.config.*;
import cy.cav.service.domain.*;
import org.slf4j.*;

import java.time.*;
import java.util.*;

/**
 * Prefecture actor that manages beneficiary actors.
 * <p>
 * Responsibilities:
 * - Creates new beneficiary actors (spawns BeneficiaryActor)
 * - Maintains registry of beneficiary actors (UUID → ActorAddress)
 * - Routes messages to the correct beneficiary actors
 * - Checks if beneficiaries exist
 * <p>
 * This replaces the old Beneficiary actor which managed all beneficiaries in a Map.
 * Now each beneficiary has its own actor (BeneficiaryActor).
 */
public class Prefecture extends Actor {
    private static final Logger log = LoggerFactory.getLogger(Prefecture.class);

    // Registry: maps beneficiary UUID to their actor address
    private final Map<UUID, ActorAddress> beneficiaryActors = new HashMap<>();

    // Store for persistence (UI display and fallback)
    private final Store store;

    // To have an initial list of beneficiaries
    private final DefaultBeneficiaries defaultBeneficiaries;
    private final ServerFinder serverFinder;

    private LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

    private final AckRetryer retryer = AckRetryer.constantDelay(this, Duration.ofSeconds(15)).maxRetries(10000);

    static final Router<Prefecture> router = new Router<Prefecture>()
            .route(CreateAccountRequest.class, Prefecture::createAccount)
            .route(NextMonthRequest.class, Prefecture::nextMonth)
            .route(PrefectureStateRequest.class, Prefecture::getState);

    public Prefecture(ActorInit init, Store store,
                      DefaultBeneficiaries defaultBeneficiaries,
                      ServerFinder serverFinder) {
        super(init);
        this.store = store;
        this.defaultBeneficiaries = defaultBeneficiaries;
        this.serverFinder = serverFinder;
    }

    @Override
    protected void spawned() {
        // Make some default actors
        for (Beneficiary beneficiary : defaultBeneficiaries.getDefaultBeneficiaries()) {
            ActorAddress actorAddress = world.spawn(init -> new BeneficiaryActor(init, currentMonth, beneficiary, serverFinder));
            beneficiaryActors.put(beneficiary.getId(), actorAddress);

            send(actorAddress, new RequestAllowanceRequest(AllowanceType.RSA));

            log.info("Spawned default beneficiary actor: {}", actorAddress);
        }

        send(address, new NextMonthRequest());
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
                request.phoneNumber(),
                request.address(),
                request.hasHousing(),
                request.inCouple(),
                request.numberOfDependents(),
                request.monthlyIncome(),
                request.iban(),
                LocalDate.now()
        );

        // Spawn a new BeneficiaryActor for this beneficiary
        ActorAddress actorAddress = world.spawn(init -> new BeneficiaryActor(init, currentMonth, beneficiary, serverFinder));
        beneficiaryActors.put(beneficiary.getId(), actorAddress);

        log.info("BeneficiaryActor spawned (ID: {}, Actor: {})", beneficiary.getId(), actorAddress);

        return new CreateAccountResponse(
                beneficiary.getId(),
                actorAddress
        );
    }

    private NextMonthResponse nextMonth(NextMonthRequest nextMonthRequest) {
        for (ActorAddress value : beneficiaryActors.values()) {
            retryer.send(value, new PayAllowances(currentMonth, UUID.randomUUID()));
        }

        LocalDate prevMonth = currentMonth;
        currentMonth = currentMonth.plusMonths(1);
        log.info("Prefecture switched from month {} to {}", prevMonth, currentMonth);

        return new NextMonthResponse(currentMonth);
    }

    /**
     * Generates a unique beneficiary number.
     */
    private String generateBeneficiaryNumber() {
        LocalDate now = LocalDate.now();
        return "CAV" + now.getYear() +
                String.format("%02d", now.getMonthValue()) +
                String.format("%02d", now.getDayOfMonth()) +
                String.format("%03d", (int) (Math.random() * 1000));
    }

    private PrefectureStateResponse getState(PrefectureStateRequest request) {
        return new PrefectureStateResponse(this.state().toString());
    }

}

