package cy.cav.protocol.allowances;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;
import cy.cav.protocol.*;

import java.util.*;

public record ReceivePayments(List<Payment> payments, UUID ackId) implements Message.Notification, Acknowledgeable {
    public record Ack(UUID ackId) implements Message.Notification, Acknowledgeable {}
}
