package cy.cav.protocol;

/// All special actor numbers are listed here.
public final class KnownActors {
    private KnownActors() {}

    public static final Long GREETER = 1L;
    
    // Allowance calculators
    public static final Long RSA_CALCULATOR = 101L;
    
    // Actors
    public static final Long PREFECTURE = 100L;  // manages beneficiary actors
}
