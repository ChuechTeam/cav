package cy.cav.protocol;

import cy.cav.framework.*;

/**
 * Allocation types managed
 */
public enum AllowanceType {
    RSA(KnownActors.RSA_CALCULATOR);

    private final long calculatorActorNumber;

    AllowanceType(long calculatorActorNumber) { this.calculatorActorNumber = calculatorActorNumber; }

    public long calculatorActorNumber() { return calculatorActorNumber; }

    public ActorAddress calculatorActor(Server server) {
        return server.address(calculatorActorNumber);
    }
}

