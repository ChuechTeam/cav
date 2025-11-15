package cy.cav.protocol.accounts;

import cy.cav.framework.Message;

import java.util.UUID;

/**
 * Request to check if an allocataire account exists.
 * Sent from client to GestionnaireCompte (service).
 * 
 * Demande de vérification de l'existence d'un compte allocataire.
 */
public record CheckAccountExistsRequest(
    UUID allocataireId
) implements Message.Request<CheckAccountExistsResponse> {}

