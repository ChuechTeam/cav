package cy.cav.protocol;

/// All special actor numbers are listed here.
public final class KnownActors {
    private KnownActors() {}

    public static final Long GREETER = 1L;
    
    // Calculateurs d'allocations
    // Allocation calculators
    public static final Long CALCULATEUR_RSA = 101L;
    public static final Long CALCULATEUR_ARE = 102L;
    public static final Long CALCULATEUR_APL = 103L;
    public static final Long CALCULATEUR_PRIME_ACTIVITE = 104L;
    
    // Gestionnaires
    // Managers
    public static final Long GESTIONNAIRE_COMPTE = 100L;  // Account manager (allocataires)
    public static final Long GESTIONNAIRE_DEMANDES = 105L;  // Demand manager
    public static final Long GESTIONNAIRE_VERSEMENTS = 110L;
    public static final Long DETECTEUR_ANOMALIES = 111L;
    public static final Long GESTIONNAIRE_RECLAMATIONS = 112L;
}
