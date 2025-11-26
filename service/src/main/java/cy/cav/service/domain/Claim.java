package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a claim from a beneficiary.
 */
public class Claim {
    private UUID id;
    private UUID beneficiaryId;           // Reference to beneficiary
    private UUID allowanceId;             // Reference to concerned allowance (optional)
    private String type;                  // Type: CALCULATION_ERROR, MISSING_PAYMENT, OVERPAYMENT, OTHER
    private String description;           // Claim description
    private LocalDate claimDate;          // Claim date
    private String status;                // Status: PENDING, IN_PROGRESS, RESOLVED, REJECTED
    private String resolution;            // Resolution (if resolved)
    
    // Default constructor
    public Claim() {
        this.id = UUID.randomUUID();
        this.claimDate = LocalDate.now();
        this.status = "PENDING";
    }
    
    // Constructor with essential parameters
    public Claim(UUID beneficiaryId, String type, String description) {
        this();
        this.beneficiaryId = beneficiaryId;
        this.type = type;
        this.description = description;
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
    
    public UUID getAllowanceId() {
        return allowanceId;
    }
    
    public void setAllowanceId(UUID allowanceId) {
        this.allowanceId = allowanceId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getClaimDate() {
        return claimDate;
    }
    
    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getResolution() {
        return resolution;
    }
    
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}

