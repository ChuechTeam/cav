package cy.cav.client.controller;

import cy.cav.framework.Network;
import cy.cav.framework.Server;
import cy.cav.framework.World;
import cy.cav.framework.ActorAddress;
import cy.cav.protocol.KnownActors;
import cy.cav.protocol.PrefectureInfo;
import cy.cav.protocol.accounts.CreateAccountRequest;
import cy.cav.protocol.accounts.CreateAccountResponse;
import cy.cav.protocol.requests.PrefectureStateRequest;
import cy.cav.protocol.requests.PrefectureStateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prefectures")
@CrossOrigin(origins = "*")
public class PrefectureController {

    private final World world;
    private final Network network; // Nous avons besoin du réseau pour trouver le serveur

    @Autowired
    public PrefectureController(World world, Network network) {
        this.world = world;
        this.network = network;
    }

    // Méthode utilitaire pour construire l'adresse de la préfecture
    // Elle cherche le premier serveur disponible qui n'est pas le client lui-même (si possible)
    // TODO Modifier comment on trouve l'adresse de la prefecture
    private ActorAddress resolvePrefectureAddress(Long targetServerId) {
        // IMPORTANT : On utilise targetServerId comme ID de SERVEUR
        // et KnownActors.PREFECTURE (100) comme ID d'ACTEUR.
        if (!network.servers().containsKey(targetServerId)) {
            throw new RuntimeException("Serveur introuvable !");
        }

        return new ActorAddress(targetServerId, KnownActors.PREFECTURE);
    }

    // --- 1. Lister les préfectures (Filtré par métadonnées) ---
    @GetMapping
    public List<PrefectureInfo> listPrefectures() {
        return network.servers().values().stream()
                // FILTRE AJOUTÉ : On ne garde que les serveurs configurés comme Préfecture
                .filter(server -> "true".equalsIgnoreCase(server.metadata().get("supportsPrefecture")))
                .map(server -> new PrefectureInfo(
                        server.id(), // On utilise l'ID du serveur pour le contacter plus tard
                        "Préfecture " + server.appName() + " (" + server.idString() + ")"
                ))
                .collect(Collectors.toList());
    }

    // --- 2. Récupérer l'état d'une préfecture ---
    @GetMapping("/{id}/state")
    public ResponseEntity<String> getPrefectureState(@PathVariable Long id) {
        try {
            // CORRECTION : On construit l'adresse avec le serveur trouvé
            ActorAddress target = resolvePrefectureAddress(id);

            PrefectureStateResponse response = world.<PrefectureStateResponse>query(null, target, new PrefectureStateRequest())
                    .toCompletableFuture()
                    .get(5, TimeUnit.SECONDS);

            return ResponseEntity.ok(response.toString());

        } catch (TimeoutException e) {
            return ResponseEntity.ok("UNREACHABLE");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // --- 3. Créer un compte dans une préfecture ---
    // TODO A TESTER
    @PostMapping("/{id}/accounts")
    public ResponseEntity<CreateAccountResponse> createAccount(
            @PathVariable Long id,
            @RequestBody CreateAccountRequest request) {

        try {
            // CORRECTION : On construit l'adresse avec le serveur trouvé
            ActorAddress target = resolvePrefectureAddress(id);

            CreateAccountResponse response = world.<CreateAccountResponse>query(null, target, request)
                    .toCompletableFuture()
                    .get(10, TimeUnit.SECONDS);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}