package cy.cav.protocol.allowances;

import cy.cav.framework.*;
import cy.cav.framework.reliable.*;

import java.time.*;
import java.util.*;

public record PayAllowances(LocalDate month, UUID ackId) implements Message.Notification, Acknowledgeable {
    public record Ack(UUID ackId) implements Notification, Acknowledgeable {
    }
}

