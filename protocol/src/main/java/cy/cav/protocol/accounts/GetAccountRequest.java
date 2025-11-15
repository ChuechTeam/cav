package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to get an allocataire account by ID.
 * Sent from client to GestionnaireCompte (service).
 * 
 * Demande de récupération d'un compte allocataire par ID.
 */
public record GetAccountRequest(
    UUID allocataireId
) implements Message.Request<GetAccountResponse> {}

