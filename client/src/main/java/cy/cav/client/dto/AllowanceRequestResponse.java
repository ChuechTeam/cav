package cy.cav.client.dto;

import java.time.LocalDate;
import java.util.UUID;

// Response after allowance request
public record AllowanceRequestResponse(
    UUID requestId,
    String status,
    UUID allowanceId,
    Double monthlyAmount,
    String rejectionReason,
    LocalDate requestDate
) {}

