package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a detected anomaly (trop perçu, moins perçu, erreur de calcul)
 */
public class Anomalie {
    private UUID id;
    private UUID allocationId;            // Référence à l'allocation concernée
    private UUID allocataireId;           // Référence à l'allocataire
    private AnomalyType type;             // Trop perçu, moins perçu, erreur de calcul
    private double expectedAmount;        // Montant attendu 
    private double actualAmount;          // Montant réellement versé 
    private double difference;            // Différence
    private LocalDate detectionDate;      // Date de détection
    private boolean resolved;             // Anomalie résolue ou non
    
    // Constructeur par défaut
    public Anomalie() {
        this.id = UUID.randomUUID();
        this.detectionDate = LocalDate.now();
        this.resolved = false;
    }
    
    // Constructeur avec paramètres essentiels
    public Anomalie(UUID allocationId, UUID allocataireId, AnomalyType type,
                   double expectedAmount, double actualAmount) {
        this();
        this.allocationId = allocationId;
        this.allocataireId = allocataireId;
        this.type = type;
        this.expectedAmount = expectedAmount;
        this.actualAmount = actualAmount;
        this.difference = Math.abs(expectedAmount - actualAmount);
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

