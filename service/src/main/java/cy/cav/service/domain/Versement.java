package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a payment (paiement d'allocation)
 */
public class Versement {
    private UUID id;
    private UUID allocationId;            // Référence à l'allocation
    private UUID allocataireId;           // Référence à l'allocataire
    private double amount;                // Montant versé
    private LocalDate paymentDate;        // Date du versement
    private PaymentStatus status;         // Statut du versement
    private String failureReason;         // Raison d'échec si status = FAILED
    
    // Constructeur par défaut
    public Versement() {
        this.id = UUID.randomUUID();
        this.status = PaymentStatus.SCHEDULED;
    }
    
    // Constructeur avec paramètres essentiels
    public Versement(UUID allocationId, UUID allocataireId, double amount, LocalDate paymentDate) {
        this();
        this.allocationId = allocationId;
        this.allocataireId = allocataireId;
        this.amount = amount;
        this.paymentDate = paymentDate;
    }
    
    
    // Getters et Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAllocationId() {
        return allocationId;
    }
    
    public void setAllocationId(UUID allocationId) {
        this.allocationId = allocationId;
    }
    
    public UUID getAllocataireId() {
        return allocataireId;
    }
    
    public void setAllocataireId(UUID allocataireId) {
        this.allocataireId = allocataireId;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}

