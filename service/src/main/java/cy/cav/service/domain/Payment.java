package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a payment (allowance payment)
 */
public class Payment {
    private UUID id;
    private UUID allowanceId;            // Reference to allowance
    private UUID beneficiaryId;          // Reference to beneficiary
    private double amount;                // Paid amount
    private LocalDate paymentDate;       // Payment date
    private PaymentStatus status;         // Payment status
    private String failureReason;         // Failure reason if status = FAILED
    
    // Default constructor
    public Payment() {
        this.id = UUID.randomUUID();
        this.status = PaymentStatus.SCHEDULED;
    }
    
    // Constructor with essential parameters
    public Payment(UUID allowanceId, UUID beneficiaryId, double amount, LocalDate paymentDate) {
        this();
        this.allowanceId = allowanceId;
        this.beneficiaryId = beneficiaryId;
        this.amount = amount;
        this.paymentDate = paymentDate;
    }
    
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAllowanceId() {
        return allowanceId;
    }
    
    public void setAllowanceId(UUID allowanceId) {
        this.allowanceId = allowanceId;
    }
    
    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }
    
    public void setBeneficiaryId(UUID beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
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

