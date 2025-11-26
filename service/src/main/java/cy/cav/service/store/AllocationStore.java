package cy.cav.service.store;

import cy.cav.service.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for allocations, payments and anomalies
 * Uses ConcurrentHashMap for thread-safety (on peut accédee en même temps par plusieurs threads sans conflit)
 */
@Component
public class AllocationStore {
    // Stores for different entities
    private final Map<UUID, Beneficiary> beneficiaries = new ConcurrentHashMap<>();
    private final Map<UUID, AllocationActive> allocations = new ConcurrentHashMap<>();
    private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();
    private final Map<UUID, Anomaly> anomalies = new ConcurrentHashMap<>();
    private final Map<UUID, AllowanceRequest> requests = new ConcurrentHashMap<>();
    private final Map<UUID, Claim> claims = new ConcurrentHashMap<>();
    
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
    
    // ========== Allocations ==========
    
    // idem

    public void saveAllocation(AllocationActive allocation) {
        allocations.put(allocation.getId(), allocation);
    }
    
    
    public Optional<AllocationActive> findAllocationById(UUID id) {
        return Optional.ofNullable(allocations.get(id));
    }
    
    // Get all active allocations for a specific beneficiary
    public List<AllocationActive> findAllocationsByBeneficiaryId(UUID beneficiaryId) {
        return allocations.values().stream()
                .filter(a -> a.getBeneficiaryId().equals(beneficiaryId))
                .filter(a -> a.getStatus() == AllocationStatus.ACTIVE)
                .collect(Collectors.toList());
    }
    

    public List<AllocationActive> findAllActiveAllocations() {
        return allocations.values().stream()
                .filter(a -> a.getStatus() == AllocationStatus.ACTIVE)
                .collect(Collectors.toList());
    }
    
    // ========== Payments ==========
    
    public void savePayment(Payment payment) {
        payments.put(payment.getId(), payment);
    }

    public Optional<Payment> findPaymentById(UUID id) {
        return Optional.ofNullable(payments.get(id));
    }

    public List<Payment> findPaymentsByAllocationId(UUID allocationId) {
        return payments.values().stream()
                .filter(p -> p.getAllowanceId().equals(allocationId))
                .collect(Collectors.toList());
    }
    
    // Get all completed payments for a specific allocation
    public List<Payment> findCompletedPaymentsByAllocationId(UUID allocationId) {
        return payments.values().stream()
                .filter(p -> p.getAllowanceId().equals(allocationId))
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());
    }
    
    // All payments for a specific beneficiary
    public List<Payment> findPaymentsByBeneficiaryId(UUID beneficiaryId) {
        return payments.values().stream()
                .filter(p -> p.getBeneficiaryId().equals(beneficiaryId))
                .collect(Collectors.toList());
    }
    
    // ========== Anomalies ==========
    
    // Create an anomaly
    public void saveAnomaly(Anomaly anomaly) {
        anomalies.put(anomaly.getId(), anomaly);
    }

    public Optional<Anomaly> findAnomalyById(UUID id) {
        return Optional.ofNullable(anomalies.get(id));
    }
    
    // All unresolved anomalies
    public List<Anomaly> findUnresolvedAnomalies() {
        return anomalies.values().stream()
                .filter(a -> !a.isResolved())
                .collect(Collectors.toList());
    }
    
    public List<Anomaly> findAnomaliesByBeneficiaryId(UUID beneficiaryId) {
        return anomalies.values().stream()
                .filter(a -> a.getBeneficiaryId().equals(beneficiaryId))
                .collect(Collectors.toList());
    }
    
    // ========== Allowance Requests ==========
    
    public void saveRequest(AllowanceRequest request) {
        requests.put(request.getId(), request);
    }
    
    public Optional<AllowanceRequest> findRequestById(UUID id) {
        return Optional.ofNullable(requests.get(id));
    }
    
    public List<AllowanceRequest> findRequestsByBeneficiaryId(UUID beneficiaryId) {
        return requests.values().stream()
                .filter(r -> r.getBeneficiaryId().equals(beneficiaryId))
                .collect(Collectors.toList());
    }
    
    public List<AllowanceRequest> findPendingRequests() {
        return requests.values().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }
    
    // ========== Claims ==========
    
    public void saveClaim(Claim claim) {
        claims.put(claim.getId(), claim);
    }
    
    public Optional<Claim> findClaimById(UUID id) {
        return Optional.ofNullable(claims.get(id));
    }
    
    public List<Claim> findClaimsByBeneficiaryId(UUID beneficiaryId) {
        return claims.values().stream()
                .filter(c -> c.getBeneficiaryId().equals(beneficiaryId))
                .collect(Collectors.toList());
    }
    
    public List<Claim> findPendingClaims() {
        return claims.values().stream()
                .filter(c -> "PENDING".equals(c.getStatus()))
                .collect(Collectors.toList());
    }
    
    // ========== Utilities ==========
    
    // Clear all stores (for tests)
    public void clearAll() {
        beneficiaries.clear();
        allocations.clear();
        payments.clear();
        anomalies.clear();
        requests.clear();
        claims.clear();
    }
    
    // Count total number of beneficiaries
    public int countBeneficiaries() {
        return beneficiaries.size();
    }
    
    // nb d'allocations actives
    public int countActiveAllocations() {
        return (int) allocations.values().stream()
                .filter(a -> a.getStatus() == AllocationStatus.ACTIVE)
                .count();
    }
}

