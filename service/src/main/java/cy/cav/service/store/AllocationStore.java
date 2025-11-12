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
    // Stores pour les différentes entités
    private final Map<UUID, Allocataire> allocataires = new ConcurrentHashMap<>();
    private final Map<UUID, AllocationActive> allocations = new ConcurrentHashMap<>();
    private final Map<UUID, Versement> versements = new ConcurrentHashMap<>();
    private final Map<UUID, Anomalie> anomalies = new ConcurrentHashMap<>();
    
    // ========== Allocataires ==========
    
    // Sauvegarde un allocataire
    public void saveAllocataire(Allocataire allocataire) {
        allocataires.put(allocataire.getId(), allocataire);
    }
    
    // Recup allocataire par son ID
    public Optional<Allocataire> findAllocataireById(UUID id) {
        return Optional.ofNullable(allocataires.get(id));
    }
    
    // Recup tous les allocataires
    public List<Allocataire> findAllAllocataires() {
        return new ArrayList<>(allocataires.values());
    }
    
    // ========== Allocations ==========
    
    // idem

    public void saveAllocation(AllocationActive allocation) {
        allocations.put(allocation.getId(), allocation);
    }
    
    
    public Optional<AllocationActive> findAllocationById(UUID id) {
        return Optional.ofNullable(allocations.get(id));
    }
    
    // Recup toutes les allocations actives d'un allocataire spécifique
    public List<AllocationActive> findAllocationsByAllocataireId(UUID allocataireId) {
        return allocations.values().stream()
                .filter(a -> a.getAllocataireId().equals(allocataireId))
                .filter(a -> a.getStatus() == AllocationStatus.ACTIVE)
                .collect(Collectors.toList());
    }
    

    public List<AllocationActive> findAllActiveAllocations() {
        return allocations.values().stream()
                .filter(a -> a.getStatus() == AllocationStatus.ACTIVE)
                .collect(Collectors.toList());
    }
    
    // ========== Versements ==========
    
    
    public void saveVersement(Versement versement) {
        versements.put(versement.getId(), versement);
    }
    

    public Optional<Versement> findVersementById(UUID id) {
        return Optional.ofNullable(versements.get(id));
    }
    

    public List<Versement> findVersementsByAllocationId(UUID allocationId) {
        return versements.values().stream()
                .filter(v -> v.getAllocationId().equals(allocationId))
                .collect(Collectors.toList());
    }
    
    // Recup tous les versements effectués (COMPLETED) d'une allocation spécifique
    public List<Versement> findCompletedVersementsByAllocationId(UUID allocationId) {
        return versements.values().stream()
                .filter(v -> v.getAllocationId().equals(allocationId))
                .filter(v -> v.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());
    }
    
    // tous les versements d'un allocataire spécifique
    public List<Versement> findVersementsByAllocataireId(UUID allocataireId) {
        return versements.values().stream()
                .filter(v -> v.getAllocataireId().equals(allocataireId))
                .collect(Collectors.toList());
    }
    
    // ========== Anomalies ==========
    
    // crée une anomalie
    public void saveAnomalie(Anomalie anomalie) {
        anomalies.put(anomalie.getId(), anomalie);
    }
    

    public Optional<Anomalie> findAnomalieById(UUID id) {
        return Optional.ofNullable(anomalies.get(id));
    }
    

    // toutes les anomalies non résolues
    public List<Anomalie> findUnresolvedAnomalies() {
        return anomalies.values().stream()
                .filter(a -> !a.isResolved())
                .collect(Collectors.toList());
    }
    
    
    public List<Anomalie> findAnomaliesByAllocataireId(UUID allocataireId) {
        return anomalies.values().stream()
                .filter(a -> a.getAllocataireId().equals(allocataireId))
                .collect(Collectors.toList());
    }
    
    // ========== Utilitaires ==========
    
    // vide tous les stores (pour les tests)
    public void clearAll() {
        allocataires.clear();
        allocations.clear();
        versements.clear();
        anomalies.clear();
    }
    
    // compte le nombre total d'allocataires
    public int countAllocataires() {
        return allocataires.size();
    }
    
    // nb d'allocations actives
    public int countActiveAllocations() {
        return (int) allocations.values().stream()
                .filter(a -> a.getStatus() == AllocationStatus.ACTIVE)
                .count();
    }
}

