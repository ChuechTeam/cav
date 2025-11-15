package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response after creating an allocataire account.
 * Retourné par GestionnaireCompte après création d'un compte.
 */
public record CreateAccountResponse(
    UUID allocataireId,
    String allocataireNumber,
    LocalDate registrationDate,
    String status
) implements Message.Response {}

