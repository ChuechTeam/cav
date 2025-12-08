package cy.cav.service.store;

import cy.cav.service.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// todo: rename
/**
 * In-memory store for allowances, payments and anomalies
 * Uses ConcurrentHashMap for thread-safety (on peut accédee en même temps par plusieurs threads sans conflit)
 */
@Component
public class AllocationStore {
    // Stores for different entities
    private final Map<UUID, Beneficiary> beneficiaries = new ConcurrentHashMap<>();
    
    // ========== Beneficiaries ==========
    
    // Save a beneficiary
    public void saveBeneficiary(Beneficiary beneficiary) {
        beneficiaries.put(beneficiary.getId(), beneficiary);
    }
    
    // Get beneficiary by ID
    public Optional<Beneficiary> findBeneficiaryById(UUID id) {
        return Optional.ofNullable(beneficiaries.get(id));
    }
    
    // Get all beneficiaries
    public List<Beneficiary> findAllBeneficiaries() {
        return new ArrayList<>(beneficiaries.values());
    }

    // ========== Utilities ==========
    
    // Clear all stores (for tests)
    public void clearAll() {
        beneficiaries.clear();
    }
    
    // Count total number of beneficiaries
    public int countBeneficiaries() {
        return beneficiaries.size();
    }
}

