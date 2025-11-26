package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents an allowance request.
 */
public class AllowanceRequest {
    private UUID id;
    private UUID beneficiaryId;           // Reference to beneficiary
    private String allowanceType;        // Requested allowance type (RSA, ARE, APL, PRIME_ACTIVITE)
    private LocalDate requestDate;        // Request date
    private String status;                // Status: PENDING, ACCEPTED, REJECTED
    private UUID allowanceId;            // ID of created allowance (if accepted)
    private Double monthlyAmount;         // Monthly amount granted (if accepted)
    private String rejectionReason;      // Rejection reason (if rejected)
    
    // Default constructor
    public AllowanceRequest() {
        this.id = UUID.randomUUID();
        this.requestDate = LocalDate.now();
        this.status = "PENDING";
    }
    
    // Constructor with essential parameters
    public AllowanceRequest(UUID beneficiaryId, String allowanceType) {
        this();
        this.beneficiaryId = beneficiaryId;
        this.allowanceType = allowanceType;
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
    
    public String getAllowanceType() {
        return allowanceType;
    }
    
    public void setAllowanceType(String allowanceType) {
        this.allowanceType = allowanceType;
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
    
    public UUID getAllowanceId() {
        return allowanceId;
    }
    
    public void setAllowanceId(UUID allowanceId) {
        this.allowanceId = allowanceId;
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

