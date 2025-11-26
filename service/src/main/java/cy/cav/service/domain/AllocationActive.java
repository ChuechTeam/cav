package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

// Represents an active allowance (currently being paid)

public class AllocationActive {
    private UUID id;
    private UUID beneficiaryId;           // Reference to beneficiary
    private AllocationType type;           // Type d'allocation (RSA, ARE, etc.)
    private AllocationStatus status;      // Statut (ACTIVE, SUSPENDED, TERMINATED)
    private double monthlyAmount;         // Montant mensuel
    private LocalDate startDate;          // Date de début
    private LocalDate endDate;            // Date de fin (null si indéterminée)
    private LocalDate lastPaymentDate;    // Date du dernier versement
    
    // Default constructor
    public AllocationActive() {
        this.id = UUID.randomUUID();
        this.status = AllocationStatus.ACTIVE;
    }
    
    // Constructor with essential parameters
    public AllocationActive(UUID beneficiaryId, AllocationType type, double monthlyAmount) {
        this();
        this.beneficiaryId = beneficiaryId;
        this.type = type;
        this.monthlyAmount = monthlyAmount;
        this.startDate = LocalDate.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }
    
    public void setBeneficiaryId(UUID beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
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

