package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

// Represents an active allocation (en cours de versement)

public class AllocationActive {
    private UUID id;
    private UUID allocataireId;           // Référence à l'allocataire
    private AllocationType type;           // Type d'allocation (RSA, ARE, etc.)
    private AllocationStatus status;      // Statut (ACTIVE, SUSPENDED, TERMINATED)
    private double monthlyAmount;         // Montant mensuel
    private LocalDate startDate;          // Date de début
    private LocalDate endDate;            // Date de fin (null si indéterminée)
    private LocalDate lastPaymentDate;    // Date du dernier versement
    
    // Constructeur par défaut
    public AllocationActive() {
        this.id = UUID.randomUUID();
        this.status = AllocationStatus.ACTIVE;
    }
    
    // Constructeur avec paramètres essentiels
    public AllocationActive(UUID allocataireId, AllocationType type, double monthlyAmount) {
        this();
        this.allocataireId = allocataireId;
        this.type = type;
        this.monthlyAmount = monthlyAmount;
        this.startDate = LocalDate.now();
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
    
    public AllocationType getType() {
        return type;
    }
    
    public void setType(AllocationType type) {
        this.type = type;
    }
    
    public AllocationStatus getStatus() {
        return status;
    }
    
    public void setStatus(AllocationStatus status) {
        this.status = status;
    }
    
    public double getMonthlyAmount() {
        return monthlyAmount;
    }
    
    public void setMonthlyAmount(double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }
    
    public void setLastPaymentDate(LocalDate lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }
}

