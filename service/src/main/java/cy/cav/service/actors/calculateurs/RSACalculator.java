package cy.cav.service.actors.calculateurs;

import cy.cav.framework.*;
import cy.cav.protocol.allocations.CalculateRSARequest;
import cy.cav.protocol.allocations.DecisionAllocationResponse;
import cy.cav.service.domain.*;
import cy.cav.service.store.AllocationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Calculates RSA allocation amounts (calcule les montants d'allocation RSA)
public class RSACalculator extends Actor {
    private static final Logger log = LoggerFactory.getLogger(RSACalculator.class);
    
    private final AllocationStore store;
    
    // Base amounts for RSA (montants de base RSA)
    private static final double BASE_AMOUNT_SINGLE = 600.0;      // Personne seule
    private static final double BASE_AMOUNT_COUPLE = 900.0;       // Couple
    private static final double AMOUNT_PER_CHILD = 240.0;         // Par enfant
    private static final double HOUSING_DEDUCTION_SINGLE = 70.0;  // Forfait logement 1 personne
    private static final double HOUSING_DEDUCTION_COUPLE = 140.0; // Forfait logement couple
    private static final double HOUSING_DEDUCTION_FAMILY = 180.0; // Forfait logement 3+ personnes
    
    // Error rate for simulation (taux d'erreur pour simulation d'anomalies)
    private static final double ERROR_RATE = 0.05; // 5% chance of calculation error
    
    static final Router<RSACalculator> router = new Router<RSACalculator>()
        .route(CalculateRSARequest.class, RSACalculator::calculateRSA);
    
    public RSACalculator(ActorInit init, AllocationStore store) {
        super(init);
        this.store = store;
    }
    
    @Override
    protected void process(Envelope<?> envelope) {
        router.process(this, envelope);
    }
    
    // Processes RSA calculation request (traite une demande de calcul RSA)
    DecisionAllocationResponse calculateRSA(CalculateRSARequest request) {
        log.info("RSA calculation requested for beneficiary: {}", request.beneficiaryId());
        
        // Always register beneficiary in service store for tracking
        // Even if request is rejected, we keep track for future claims
        ensureBeneficiaryExists(request);
        
        // Check eligibility using request data (vérification d'éligibilité simplifiée)
        boolean eligible = checkEligibility(request);
        String rejectionReason = null;
        
        double roundedAmount = 0.0;
        
        if (eligible) {
            // Calculate amount (calcul du montant)
            double calculatedAmount = calculateRSAAmount(request);
            
            // Simulate calculation error (simulation d'erreur de calcul pour anomalies)
            double finalAmount = applyErrorIfNeeded(calculatedAmount);
            
            // Round to nearest 10€ (arrondi à 10€ près)
            roundedAmount = Math.round(finalAmount / 10.0) * 10.0;
            
            log.info("RSA amount calculated: {}€ (rounded: {}€) for beneficiary: {}", 
                calculatedAmount, roundedAmount, request.beneficiaryId());
        } else {
            rejectionReason = "Eligibility conditions not met";
            log.info("RSA request rejected for beneficiary: {} - {}", request.beneficiaryId(), rejectionReason);
        }
        
        // Always create allocation for tracking
        // Even if rejected, we keep it for future claims
        AllocationActive allocation = new AllocationActive(
            request.beneficiaryId(),
            AllocationType.RSA,
            roundedAmount
        );
        
        // Set status: TERMINATED if rejected, ACTIVE if accepted (statut selon acceptation)
        if (!eligible || roundedAmount == 0) {
            allocation.setStatus(AllocationStatus.TERMINATED);
            allocation.setEndDate(java.time.LocalDate.now());
        }
        
        // Save to store (always, even if rejected)
        store.saveAllocation(allocation);
        
        log.info("RSA allocation created (status: {}): {} for beneficiary: {}", 
            allocation.getStatus(), allocation.getId(), request.beneficiaryId());
        
        return new DecisionAllocationResponse(
            eligible && roundedAmount > 0,
            roundedAmount,
            rejectionReason,
            allocation.getId(),
            "RSA"
        );
    }
    
    // Ensures beneficiary exists in service store for tracking
    private void ensureBeneficiaryExists(CalculateRSARequest request) {
        if (!store.findBeneficiaryById(request.beneficiaryId()).isPresent()) {
            // Create minimal beneficiary for tracking
            // We don't have all fields, but we track the ID and basic info from request
            cy.cav.service.domain.Beneficiary beneficiary = new cy.cav.service.domain.Beneficiary();
            beneficiary.setId(request.beneficiaryId());
            beneficiary.setInCouple(request.inCouple());
            beneficiary.setNumberOfDependents(request.numberOfDependents());
            beneficiary.setMonthlyIncome(request.monthlyIncome());
            
            store.saveBeneficiary(beneficiary);
            log.debug("Beneficiary registered in service for tracking: {}", request.beneficiaryId());
        }
    }
    
    // Checks eligibility (vérifie l'éligibilité simplifiée)
    private boolean checkEligibility(CalculateRSARequest request) {
        // Simplified eligibility check (vérification simplifiée)
        // In real scenario, would need birthDate to check age >= 25
        // For now: eligible if has dependents or if amount would be > 0
        if (request.numberOfDependents() > 0) {
            return true;
        }
        
        // Check if calculated amount would be > 0 (quick check)
        double quickAmount = BASE_AMOUNT_SINGLE - request.monthlyIncome();
        if (request.hasHousing()) {
            quickAmount -= HOUSING_DEDUCTION_SINGLE;
        }
        
        // For now, accept if amount would be positive (simplified)
        // In production, would need proper age verification
        return quickAmount > 0;
    }
    
    // Calculates RSA amount (calcule le montant RSA)
    private double calculateRSAAmount(CalculateRSARequest request) {
        // Base amount according to household composition
        double baseAmount;
        if (request.inCouple()) {
            baseAmount = BASE_AMOUNT_COUPLE;
        } else {
            baseAmount = BASE_AMOUNT_SINGLE;
        }
        
        // Add amount per child
        baseAmount += request.numberOfDependents() * AMOUNT_PER_CHILD;
        
        // Subtract income
        double amountAfterIncome = baseAmount - request.monthlyIncome();
        
        // Subtract housing deduction if has housing
        if (request.hasHousing()) {
            int householdSize = (request.inCouple() ? 2 : 1) + request.numberOfDependents();
            if (householdSize >= 3) {
                amountAfterIncome -= HOUSING_DEDUCTION_FAMILY;
            } else if (request.inCouple()) {
                amountAfterIncome -= HOUSING_DEDUCTION_COUPLE;
            } else {
                amountAfterIncome -= HOUSING_DEDUCTION_SINGLE;
            }
        }
        
        // RSA cannot be negative
        return Math.max(0.0, amountAfterIncome);
    }
    
    // Applies calculation error with probability ERROR_RATE (applique une erreur de calcul avec probabilité)
    private double applyErrorIfNeeded(double amount) {
        if (Math.random() < ERROR_RATE) {
            // Error: add or subtract 50-100€ randomly
            double error = (Math.random() < 0.5 ? 1 : -1) * (50 + Math.random() * 50);
            log.warn("Erreur de calcul simulée: {}€ ajouté au montant {}€", error, amount);
            return amount + error;
        }
        return amount;
    }
}

