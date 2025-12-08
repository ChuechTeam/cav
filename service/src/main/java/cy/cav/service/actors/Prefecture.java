package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.protocol.accounts.*;
import cy.cav.service.config.*;
import cy.cav.service.domain.*;
import cy.cav.service.store.*;
import org.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

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
    private final Map<UUID, ActorAddress> beneficiaryActors = new ConcurrentHashMap<>();

    // Store for persistence (UI display and fallback)
    private final AllocationStore store;

    // To have an initial list of beneficiaries
    private final DefaultBeneficiaries defaultBeneficiaries;

    static final Router<Prefecture> router = new Router<Prefecture>()
            .route(CreateAccountRequest.class, Prefecture::createAccount);

    public Prefecture(ActorInit init, AllocationStore store,
                      DefaultBeneficiaries defaultBeneficiaries) {
        super(init);
        this.store = store;
        this.defaultBeneficiaries = defaultBeneficiaries;
    }

    @Override
    protected void spawned() {
        // Make some default actors
        for (Beneficiary beneficiary : defaultBeneficiaries.getDefaultBeneficiaries()) {
            ActorAddress actorAddress = world.spawn(init -> new BeneficiaryActor(init, beneficiary, store));
            beneficiaryActors.put(beneficiary.getId(), actorAddress);

            log.info("Spawned default beneficiary actor: {}", actorAddress);
        }
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

        log.info("Account created: {} (ID: {})", beneficiaryNumber, beneficiary.getId());

        return new CreateAccountResponse(
                beneficiary.getId(),
                actorAddress
        );
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
}

