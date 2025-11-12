package cy.cav.client.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Représente une réclamation d'un allocataire.
 * Represents a claim from an allocataire.
 */
public class Reclamation {
    private UUID id;
    private UUID allocataireId;           // Référence à l'allocataire
    private UUID allocationId;             // Référence à l'allocation concernée (optionnel)
    private String type;                   // Type: CALCULATION_ERROR, MISSING_PAYMENT, OVERPAYMENT, OTHER
    private String description;            // Description de la réclamation
    private LocalDate claimDate;           // Date de la réclamation
    private String status;                 // Statut: PENDING, IN_PROGRESS, RESOLVED, REJECTED
    private String resolution;             // Résolution (si résolue)
    
    // Constructeur par défaut
    public Reclamation() {
        this.id = UUID.randomUUID();
        this.claimDate = LocalDate.now();
        this.status = "PENDING";
    }
    
    // Constructeur avec paramètres essentiels
    public Reclamation(UUID allocataireId, String type, String description) {
        this();
        this.allocataireId = allocataireId;
        this.type = type;
        this.description = description;
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
    
    public UUID getAllocationId() {
        return allocationId;
    }
    
    public void setAllocationId(UUID allocationId) {
        this.allocationId = allocationId;
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

