package cy.cav.service.actors;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cy.cav.framework.Actor;
import cy.cav.framework.ActorInit;
import cy.cav.framework.Envelope;
import cy.cav.framework.Router;
import cy.cav.framework.reliable.AckStore;
import cy.cav.protocol.AllowanceType;
import cy.cav.protocol.allowances.CalculateAllowance;

// Calculates RSA allocation amounts (calcule les montants d'allocation RSA)
public class RSACalculator extends Actor {
    private static final Logger log = LoggerFactory.getLogger(RSACalculator.class);

    // Base amounts for RSA (montants de base RSA)
    private static final BigDecimal BASE_AMOUNT_SINGLE = BigDecimal.valueOf(600.0);      // Personne seule
    private static final BigDecimal BASE_AMOUNT_COUPLE = BigDecimal.valueOf(900.0);       // Couple
    private static final BigDecimal AMOUNT_PER_CHILD = BigDecimal.valueOf(240.0);         // Par enfant
    private static final BigDecimal HOUSING_DEDUCTION_SINGLE = BigDecimal.valueOf(70.0);  // Forfait logement 1 personne
    private static final BigDecimal HOUSING_DEDUCTION_COUPLE = BigDecimal.valueOf(140.0); // Forfait logement couple
    private static final BigDecimal HOUSING_DEDUCTION_FAMILY = BigDecimal.valueOf(180.0); // Forfait logement 3+ personnes

    static final Router<RSACalculator> router = new Router<RSACalculator>()
            .route(CalculateAllowance.class, RSACalculator::calculateRSA);

    private final AckStore<CalculateAllowance.Ack> ackStore = new AckStore<>(this);

    public RSACalculator(ActorInit init) {
        super(init);
    }

    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }

    // Processes RSA calculation request (traite une demande de calcul RSA)
    void calculateRSA(Envelope<CalculateAllowance> envelope) {
        if (ackStore.sendIfAcknowledged(envelope)) {
            return;
        }

        CalculateAllowance request = envelope.body();
        log.info("RSA calculation requested for beneficiary: {}", request.profile());

        // Check eligibility using request data (vérification d'éligibilité simplifiée)
        boolean eligible = checkEligibility(request);

        CalculateAllowance.Ack message;
        if (eligible) {
            BigDecimal calculatedAmount = calculateRSAAmount(request);

            log.info("RSA amount calculated: {}€ for beneficiary: {}", calculatedAmount, request.profile());
            message = new CalculateAllowance.Ack(AllowanceType.RSA, calculatedAmount, "", request.ackId());
        } else {
            log.info("RSA request rejected for beneficiary: {}", request);
            message = new CalculateAllowance.Ack(AllowanceType.RSA, BigDecimal.ZERO,
                    "Vous n'êtes pas éligible au RSA. C'est dommage !", request.ackId());
        }

        ackStore.send(envelope.sender(), message);
    }


    // Checks eligibility (vérifie l'éligibilité simplifiée)
    private boolean checkEligibility(CalculateAllowance request) {
        // Simplified eligibility check (vérification simplifiée)
        // In real scenario, would need birthDate to check age >= 25
        // For now: eligible if has dependents or if amount would be > 0
        if (request.profile().numberOfDependents() > 0) {
            return true;
        }

        // Check if calculated amount would be > 0 (quick check)
        BigDecimal quickAmount = BASE_AMOUNT_SINGLE.subtract(request.profile().monthlyIncome());
        if (request.profile().hasHousing() != false) {
            quickAmount = quickAmount.subtract(HOUSING_DEDUCTION_SINGLE);
        }
        // For now, accept if amount would be positive (simplified)
        // In production, would need proper age verification
        return quickAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    // Calculates RSA amount (calcule le montant RSA)
    private BigDecimal calculateRSAAmount(CalculateAllowance request) {
        // Base amount according to household composition
        BigDecimal baseAmount;
        if (request.profile().inCouple()) {
            baseAmount = BASE_AMOUNT_COUPLE;
        } else {
            baseAmount = BASE_AMOUNT_SINGLE;
        }

        // Add amount per child
        baseAmount = baseAmount.add(AMOUNT_PER_CHILD.multiply(BigDecimal.valueOf(request.profile().numberOfDependents())));

        // Subtract income
        BigDecimal amountAfterIncome = baseAmount.subtract(request.profile().monthlyIncome());

        // Subtract housing deduction if has housing
        if (request.profile().hasHousing() != false) {
            int householdSize = (request.profile().inCouple() ? 2 : 1) + request.profile().numberOfDependents();
            if (householdSize >= 3) {
                amountAfterIncome = amountAfterIncome.subtract(HOUSING_DEDUCTION_FAMILY);
            } else if (request.profile().inCouple()) {
                amountAfterIncome = amountAfterIncome.subtract(HOUSING_DEDUCTION_COUPLE);
            } else {
                amountAfterIncome = amountAfterIncome.subtract(HOUSING_DEDUCTION_SINGLE);
            }
        }

        // RSA cannot be negative
        return amountAfterIncome.max(BigDecimal.ZERO);
    }
}

