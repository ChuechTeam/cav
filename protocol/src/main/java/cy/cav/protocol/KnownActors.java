package cy.cav.protocol;

/// All special actor numbers are listed here.
public final class KnownActors {
    private KnownActors() {}

    public static final Long GREETER = 1L;
    
    // Allowance calculators
    public static final Long RSA_CALCULATOR = 101L;
    public static final Long ARE_CALCULATOR = 102L;
    public static final Long APL_CALCULATOR = 103L;
    public static final Long PRIME_ACTIVITE_CALCULATOR = 104L;
    
    // Actors
    public static final Long PREFECTURE = 100L;  // manages beneficiary actors
    public static final Long ALLOWANCE_REQUEST = 105L;  // Allowance request manager
    public static final Long PAYMENT_MANAGER = 110L;
    public static final Long ANOMALY_DETECTOR = 111L;
    public static final Long CLAIM_MANAGER = 112L;
}
