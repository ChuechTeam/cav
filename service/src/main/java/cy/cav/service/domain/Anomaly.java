package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a detected anomaly (overpayment, underpayment, calculation error)
 */
public class Anomaly {
    private UUID id;
    private UUID allowanceId;            // Reference to concerned allowance
    private UUID beneficiaryId;           // Reference to beneficiary
    private AnomalyType type;             // Overpayment, underpayment, calculation error
    private double expectedAmount;        // Expected amount 
    private double actualAmount;           // Actually paid amount 
    private double difference;            // Difference
    private LocalDate detectionDate;      // Detection date
    private boolean resolved;             // Anomaly resolved or not
    
    // Default constructor
    public Anomaly() {
        this.id = UUID.randomUUID();
        this.detectionDate = LocalDate.now();
        this.resolved = false;
    }
    
    // Constructor with essential parameters
    public Anomaly(UUID allowanceId, UUID beneficiaryId, AnomalyType type,
                   double expectedAmount, double actualAmount) {
        this();
        this.allowanceId = allowanceId;
        this.beneficiaryId = beneficiaryId;
        this.type = type;
        this.expectedAmount = expectedAmount;
        this.actualAmount = actualAmount;
        this.difference = Math.abs(expectedAmount - actualAmount);
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
    
    public AnomalyType getType() {
        return type;
    }
    
    public void setType(AnomalyType type) {
        this.type = type;
    }
    
    public double getExpectedAmount() {
        return expectedAmount;
    }
    
    public void setExpectedAmount(double expectedAmount) {
        this.expectedAmount = expectedAmount;
    }
    
    public double getActualAmount() {
        return actualAmount;
    }
    
    public void setActualAmount(double actualAmount) {
        this.actualAmount = actualAmount;
    }
    
    public double getDifference() {
        return difference;
    }
    
    public void setDifference(double difference) {
        this.difference = difference;
    }
    
    public LocalDate getDetectionDate() {
        return detectionDate;
    }
    
    public void setDetectionDate(LocalDate detectionDate) {
        this.detectionDate = detectionDate;
    }
    
    public boolean isResolved() {
        return resolved;
    }
    
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}

