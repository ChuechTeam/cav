package cy.cav.service.actors;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;
import cy.cav.protocol.*;
import cy.cav.protocol.allowances.*;
import cy.cav.service.*;

import java.math.*;
import java.time.*;
import java.util.*;

public class PaymentProcess extends Actor {
    private static final Router<PaymentProcess> router = new Router<PaymentProcess>()
            .route(CalculateAllowance.Ack.class, PaymentProcess::allowanceCalculated)
            .route(ReceivePayments.Ack.class, PaymentProcess::paymentsReceived);

    private final ServerFinder serverFinder;
    private final ActorAddress beneficiaryActor;
    private final BeneficiaryProfile profile;
    private final LocalDate month;
    private final Set<AllowanceType> allowancesRemaining;

    private final List<Payment> paymentsToDistribute = new ArrayList<>();

    private final AckRetryer retryer = AckRetryer.constantDelay(this, Duration.ofSeconds(5)).maxRetries(10000);

    private final int totalAllowances;

    protected PaymentProcess(ActorInit init,
                             ServerFinder serverFinder,
                             ActorAddress beneficiaryActor,
                             BeneficiaryProfile profile,
                             LocalDate month,
                             Set<AllowanceType> wantedAllowances) {
        super(init);
        this.serverFinder = serverFinder;
        this.beneficiaryActor = beneficiaryActor;
        this.profile = profile;
        this.month = month;
        this.allowancesRemaining = wantedAllowances;
        this.totalAllowances = wantedAllowances.size();
    }

    @Override
    protected void spawned() {
        if (allowancesRemaining.isEmpty()) {
            log.warn("PaymentProcess spawned with zero allowances requested, that's wasteful, bye! (For actor {})", beneficiaryActor);
            despawn();
            return;
        }

        log.info("PaymentProcess ready for actor {}; sending calculation messages...", beneficiaryActor);
        for (AllowanceType allowanceType : allowancesRemaining) {
            retryer.send(_ -> serverFinder.pickCalculatorActor(allowanceType),
                    new CalculateAllowance(profile, UUID.randomUUID()));
        }
    }

    void allowanceCalculated(CalculateAllowance.Ack message) {
        if (!allowancesRemaining.remove(message.type())) {
            // We've already processed this.
            return;
        }

        if (message.amount().compareTo(BigDecimal.ZERO) > 0) {
            var payment = new Payment(
                    "Paiement pour l'aide " + message.type() + " du mois " + month.toString(),
                    message.amount());
            paymentsToDistribute.add(payment);

            log.info("Prepared payment for actor {} about allowance {}: {} ({}/{} allowances remaining)",
                    address, message.type(), payment, allowancesRemaining.size(), totalAllowances);
        } else {
            log.info("No payment for actor {} about allowance {} ({}/{} allowances remaining)",
                    address, message.type(), allowancesRemaining.size(), totalAllowances);
        }

        if (!paymentsToDistribute.isEmpty()) {
            log.info("All calculations complete; sending {} payments to actor {}", paymentsToDistribute.size(), address);
            retryer.send(beneficiaryActor, new ReceivePayments(paymentsToDistribute, UUID.randomUUID()));
        } else {
            log.info("All calculations complete; no payments to send to actor {}, goodbye!", address);
            despawn();
        }
    }

    void paymentsReceived(ReceivePayments.Ack ack) {
        log.info("Actor {} has received its payments, goodbye!", address);
        despawn();
    }

    @Override
    protected void process(Envelope<?> envelope) {
        // TODO: What to do when the retryer reaches its max tries?
        if (retryer.process(envelope)) {
            return;
        }

        router.process(this, envelope);
    }
}
