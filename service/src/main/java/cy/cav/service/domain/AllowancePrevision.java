package cy.cav.service.domain;

import cy.cav.protocol.*;
import jakarta.annotation.*;

import java.math.*;
import java.util.*;

public class AllowancePrevision {
    private final AllowanceType type;
    /// Acknowledgment id of the notification we've sent to the calculator actor.
    ///
    /// When null, it means the user doesn't want this allocation.
    private @Nullable UUID ackId;
    private AllowancePrevisionState state = AllowancePrevisionState.UNWANTED;
    /// Contains the last amount the calculator told us we're going to be paid.
    ///
    /// When stopped, it goes back to zero.
    private BigDecimal lastAmount = BigDecimal.ZERO;
    /// The last (rejection) message we received.
    private String lastMessage = "";

    public AllowancePrevision(AllowanceType type) { this.type = type; }

    /// Switches this prevision to a PENDING state with the ack id of the calculation message.
    public void start(UUID ackId) {
        this.state = AllowancePrevisionState.PENDING;
        this.ackId = ackId;
    }

    public void receiveResult(UUID ackId, BigDecimal amountGiven, String message) {
        if (this.ackId != ackId || this.state == AllowancePrevisionState.UNWANTED) {
            return;
        }

        this.state = AllowancePrevisionState.UP_TO_DATE;
        this.lastAmount = amountGiven;
        this.lastMessage = message;
    }

    public void stop() {
        this.state = AllowancePrevisionState.UNWANTED;
        this.ackId = null;
        this.lastAmount = BigDecimal.ZERO;
        this.lastMessage = "";
    }

    @Nullable
    public UUID getAckId() {
        return ackId;
    }

    public AllowancePrevisionState getState() {
        return state;
    }

    public BigDecimal getLastAmount() {
        return lastAmount;
    }

    public AllowanceType getType() {
        return type;
    }

    public cy.cav.protocol.AllowancePrevision toProtocol() {
        return new cy.cav.protocol.AllowancePrevision(type, state, lastAmount, lastMessage);
    }
}
