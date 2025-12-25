package cy.cav.client.controller;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.requests.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prefectures")
@CrossOrigin(origins = "*")
public class PrefectureController {

    private final World world;
    private final Network network;

    @Autowired
    public PrefectureController(World world, Network network) {
        this.world = world;
        this.network = network;
    }

    // Research the address of our prefecture
    private ActorAddress resolvePrefectureAddress(Long targetServerId) {
        if (!network.servers().containsKey(targetServerId)) {
            throw new RuntimeException("Serveur introuvable !");
        }
        // Verify if it is a prefecture
        if (!"true".equalsIgnoreCase(network.servers().get(targetServerId).metadata().get("supportsPrefecture"))) {
            throw new RuntimeException("Ce serveur n'est pas une préfecture !");
        }

        return new ActorAddress(targetServerId, KnownActors.PREFECTURE);
    }

    // Listing of prefectures
    @GetMapping
    public List<PrefectureInfo> listPrefectures() {
        return network.servers().values().stream()
                .filter(server -> "true".equalsIgnoreCase(server.metadata().get("supportsPrefecture")))
                .map(server -> new PrefectureInfo(
                        server.id(),
                        "Préfecture " + server.appName() + " (" + server.idString() + ")"
                ))
                .collect(Collectors.toList());
    }

    // Getting the state (name + current month of a prefecture)
    @GetMapping("/{id}/state")
    public ResponseEntity<PrefectureStateResponse> getPrefectureState(@PathVariable Long id) {
        try {
            ActorAddress target = resolvePrefectureAddress(id);

            // Use of query Sync directly
            PrefectureStateResponse response = world.querySync(target, new PrefectureStateRequest());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // querySync return ActorNotFoundException
            return ResponseEntity.notFound().build();
        }
    }

    // 3. Créer un compte - Version simplifiée avec querySync
    @PostMapping("/{id}/accounts")
    public ResponseEntity<CreateAccountResponse> createAccount(
            @PathVariable Long id,
            @RequestBody CreateAccountRequest request) {

        try {
            ActorAddress target = resolvePrefectureAddress(id);

            // Use of querySync
            CreateAccountResponse response = world.querySync(target, request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/next-month")
    public ResponseEntity<NextMonthResponse> nextMonth(@PathVariable Long id) {
        try {
            ActorAddress target = resolvePrefectureAddress(id);

            // On envoie le signal sans attendre de données du client (pas de Body nécessaire)
            NextMonthResponse response = world.querySync( target, new NextMonthRequest());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}