package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Représente une demande d'allocation.
 * Represents an allocation request.
 */
public class DemandeAllocation {
    private UUID id;
    private UUID allocataireId;           // Référence à l'allocataire
    private String allocationType;        // Type d'allocation demandé (RSA, ARE, APL, PRIME_ACTIVITE)
    private LocalDate requestDate;        // Date de la demande
    private String status;                // Statut: PENDING, ACCEPTED, REJECTED
    private UUID allocationId;            // ID de l'allocation créée (si acceptée)
    private Double monthlyAmount;          // Montant mensuel accordé (si acceptée)
    private String rejectionReason;       // Raison du refus (si rejetée)
    
    // Constructeur par défaut
    public DemandeAllocation() {
        this.id = UUID.randomUUID();
        this.requestDate = LocalDate.now();
        this.status = "PENDING";
    }
    
    // Constructeur avec paramètres essentiels
    public DemandeAllocation(UUID allocataireId, String allocationType) {
        this();
        this.allocataireId = allocataireId;
        this.allocationType = allocationType;
    }
    
    // Getters et Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAllocataireId() {
        return allocataireId;
    }
    
    public void setAllocataireId(UUID allocataireId) {
        this.allocataireId = allocataireId;
    }
    
    public String getAllocationType() {
        return allocationType;
    }
    
    public void setAllocationType(String allocationType) {
        this.allocationType = allocationType;
    }
    
    public LocalDate getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public UUID getAllocationId() {
        return allocationId;
    }
    
    public void setAllocationId(UUID allocationId) {
        this.allocationId = allocationId;
    }
    
    public Double getMonthlyAmount() {
        return monthlyAmount;
    }
    
    public void setMonthlyAmount(Double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}

