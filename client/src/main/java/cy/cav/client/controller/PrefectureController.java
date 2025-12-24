package cy.cav.client.controller;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.requests.*;
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
    private final Network network;

    @Autowired
    public PrefectureController(World world, Network network) {
        this.world = world;
        this.network = network;
    }

    // Finding prefecture address
    private ActorAddress resolvePrefectureAddress(Long targetServerId) {
        if (!network.servers().containsKey(targetServerId)) {
            throw new RuntimeException("Serveur introuvable !");
        }

        return new ActorAddress(targetServerId, KnownActors.PREFECTURE);
    }

    // listing of prefectures
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

    // get state of a prefecture
    @GetMapping("/{id}/state")
    public ResponseEntity<String> getPrefectureState(@PathVariable Long id) {
        try {
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

    // Create an account in a specified prefecture
    @PostMapping("/{id}/accounts")
    public ResponseEntity<CreateAccountResponse> createAccount(
            @PathVariable Long id,
            @RequestBody CreateAccountRequest request) {

        try {
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