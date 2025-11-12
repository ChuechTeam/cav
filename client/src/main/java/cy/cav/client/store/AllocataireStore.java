package cy.cav.client.store;

import cy.cav.client.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Store en mémoire pour les allocataires, demandes et réclamations côté client.
 * In-memory store for allocataires, requests and claims on client side.
 * 
 * Utilise ConcurrentHashMap pour la thread-safety.
 * Uses ConcurrentHashMap for thread-safety.
 */
@Component
public class AllocataireStore {
    // Stores pour les différentes entités
    private final Map<UUID, Allocataire> allocataires = new ConcurrentHashMap<>();
    private final Map<UUID, DemandeAllocation> demandes = new ConcurrentHashMap<>();
    private final Map<UUID, Reclamation> reclamations = new ConcurrentHashMap<>();
    
    // ========== Allocataires ==========
    
    /**
     * Sauvegarde un allocataire.
     * Saves an allocataire.
     */
    public void saveAllocataire(Allocataire allocataire) {
        allocataires.put(allocataire.getId(), allocataire);
    }
    
    /**
     * Récupère un allocataire par son ID.
     * Gets an allocataire by ID.
     */
    public Optional<Allocataire> findAllocataireById(UUID id) {
        return Optional.ofNullable(allocataires.get(id));
    }
    
    /**
     * Récupère un allocataire par son numéro d'allocataire.
     * Gets an allocataire by allocataire number.
     */
    public Optional<Allocataire> findAllocataireByNumber(String allocataireNumber) {
        return allocataires.values().stream()
                .filter(a -> a.getAllocataireNumber().equals(allocataireNumber))
                .findFirst();
    }
    
    /**
     * Récupère tous les allocataires.
     * Gets all allocataires.
     */
    public List<Allocataire> findAllAllocataires() {
        return new ArrayList<>(allocataires.values());
    }
    
    /**
     * Vérifie si un allocataire existe.
     * Checks if an allocataire exists.
     */
    public boolean existsAllocataire(UUID id) {
        return allocataires.containsKey(id);
    }
    
    // ========== Demandes ==========
    
    /**
     * Sauvegarde une demande d'allocation.
     * Saves an allocation request.
     */
    public void saveDemande(DemandeAllocation demande) {
        demandes.put(demande.getId(), demande);
    }
    
    /**
     * Récupère une demande par son ID.
     * Gets a request by ID.
     */
    public Optional<DemandeAllocation> findDemandeById(UUID id) {
        return Optional.ofNullable(demandes.get(id));
    }
    
    /**
     * Récupère toutes les demandes d'un allocataire.
     * Gets all requests for an allocataire.
     */
    public List<DemandeAllocation> findDemandesByAllocataireId(UUID allocataireId) {
        return demandes.values().stream()
                .filter(d -> d.getAllocataireId().equals(allocataireId))
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère toutes les demandes en attente.
     * Gets all pending requests.
     */
    public List<DemandeAllocation> findPendingDemandes() {
        return demandes.values().stream()
                .filter(d -> "PENDING".equals(d.getStatus()))
                .collect(Collectors.toList());
    }
    
    // ========== Réclamations ==========
    
    /**
     * Sauvegarde une réclamation.
     * Saves a claim.
     */
    public void saveReclamation(Reclamation reclamation) {
        reclamations.put(reclamation.getId(), reclamation);
    }
    
    /**
     * Récupère une réclamation par son ID.
     * Gets a claim by ID.
     */
    public Optional<Reclamation> findReclamationById(UUID id) {
        return Optional.ofNullable(reclamations.get(id));
    }
    
    /**
     * Récupère toutes les réclamations d'un allocataire.
     * Gets all claims for an allocataire.
     */
    public List<Reclamation> findReclamationsByAllocataireId(UUID allocataireId) {
        return reclamations.values().stream()
                .filter(r -> r.getAllocataireId().equals(allocataireId))
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère toutes les réclamations en attente.
     * Gets all pending claims.
     */
    public List<Reclamation> findPendingReclamations() {
        return reclamations.values().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }
    
    // ========== Utilitaires ==========
    
    /**
     * Vide tous les stores (pour les tests).
     * Clears all stores (for testing).
     */
    public void clearAll() {
        allocataires.clear();
        demandes.clear();
        reclamations.clear();
    }
    
    /**
     * Compte le nombre total d'allocataires.
     * Counts total number of allocataires.
     */
    public int countAllocataires() {
        return allocataires.size();
    }
    
    /**
     * Compte le nombre total de demandes.
     * Counts total number of requests.
     */
    public int countDemandes() {
        return demandes.size();
    }
}

