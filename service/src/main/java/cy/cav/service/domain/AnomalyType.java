package cy.cav.service.domain;

/**
 * Types anomalies
 */
public enum AnomalyType {
    OVERPAYMENT,      // Trop perçu (versements > montant dû)
    UNDERPAYMENT,     // Moins perçu (versements < montant dû)
    CALCULATION_ERROR // Erreur de calcul détectée
}

