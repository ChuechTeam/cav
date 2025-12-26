package cy.cav.client.controller;

import cy.cav.client.dto.*;
import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.protocol.accounts.*;
import cy.cav.protocol.requests.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.*;

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
    private ActorAddress resolvePrefectureAddress(String serverIdHex) {
        // Convert hex string to Long
        Long targetServerId;
        try {
            targetServerId = Long.parseUnsignedLong(serverIdHex, 16);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Format d'ID serveur invalide : " + serverIdHex);
        }

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
                        HexFormat.of().toHexDigits(server.id()),
                        "Préfecture " + server.appName() + " (" + server.idString() + ")"
                ))
                .collect(Collectors.toList());
    }

    // Getting the state (name + current month of a prefecture)
    @GetMapping("/{id}/state")
    public ResponseEntity<PrefectureStateResponse> getPrefectureState(@PathVariable String id) {
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
            @PathVariable String id,
            @RequestBody AllocataireDTO request) {

        try {
            ActorAddress target = resolvePrefectureAddress(id);

            // Use of querySync
            CreateAccountResponse response = world.querySync(target, new CreateAccountRequest(
                    request.firstName(),
                    request.lastName(),
                    request.birthDate(),
                    request.email(),
                    request.phoneNumber(),
                    request.address(),
                    request.hasHousing(),
                    request.inCouple(),
                    request.numberOfDependents(),
                    request.monthlyIncome(),
                    request.iban()
            ));

            return ResponseEntity.ok(response);
        } catch (ActorNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/next-month")
    public ResponseEntity<NextMonthResponse> nextMonth(@PathVariable String id) {
        try {
            ActorAddress target = resolvePrefectureAddress(id);

            // On envoie le signal sans attendre de données du client (pas de Body nécessaire)
            NextMonthResponse response = world.querySync(target, new NextMonthRequest());

            return ResponseEntity.ok(response);
        } catch (ActorNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}