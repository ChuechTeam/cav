package cy.cav.client;

import cy.cav.framework.*;
import cy.cav.protocol.*;
import cy.cav.protocol.accounts.CreateAccountRequest;
import cy.cav.protocol.accounts.CreateAccountResponse;
import cy.cav.protocol.requests.CreateAllowanceRequestRequest;
import cy.cav.protocol.requests.CreateAllowanceRequestResponse;
import cy.cav.protocol.requests.RequestAllowanceNotification;
import jakarta.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.boot.autoconfigure.web.ErrorProperties.Whitelabel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
class TestController {
    private final Network network;
    private final World world;

    TestController(Network network, World world) {
        this.network = network;
        this.world = world;
    }

    @GetMapping("/query")
    ResponseEntity<?> query() {
        Server server = firstServer();
        if (server == null) {
            return ResponseEntity.notFound().build();
        }

        ActorAddress actor = server.address(KnownActors.GREETER);
        HelloRequest request = new HelloRequest("World");

        HelloResponse response = world.querySync(actor, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/servers")
    ResponseEntity<?> servers() {
        return ResponseEntity.ok(network.servers().values());
    }

    @Nullable
    Server firstServer() {
        for (Server value : network.servers().values()) {
            return value;
        }
        return null;
    }


    // Test the  new OutsideSender feature with no reciever
    @GetMapping("/outside")
    ResponseEntity<?> outside() {
        Server server = firstServer();
        if (server == null) {
            return ResponseEntity.notFound().build();
        }
        HelloRequest request;
        ActorAddress actor = new ActorAddress(999,999); // Non existing actor
        request = new HelloRequest  ("This message will never be delivered");

        HelloResponse response = world.querySync(actor, request);

        return ResponseEntity.ok(response);
    }

    // Tester tout le workflow complet de demande RSA (Ajouté après changement de fonctionnement de Beneficiary et AllowanceRequest)
    @GetMapping("/test/full-rsa")
    public ResponseEntity<?> testFullRsaFlow() {

        System.out.println("\n=== TEST FULL RSA START ===");

        // 1) Récupération du serveur (cav-service)
        Server server = firstServer();
        if (server == null) {
            return ResponseEntity.status(500).body("Aucun serveur trouvé via Eureka");
        }

        // Récupération des adresses d'acteurs du service
        ActorAddress beneficiaryActor = server.address(KnownActors.BENEFICIARY);
        ActorAddress requestActor = server.address(KnownActors.ALLOWANCE_REQUEST);

        System.out.println("Actors trouvés :");
        System.out.println(" - Beneficiary : " + beneficiaryActor);
        System.out.println(" - AllowanceRequest : " + requestActor);

        // 2) Création d’un BÉNÉFICIAIRE complet
        UUID newBeneficiaryId = UUID.randomUUID();
        CreateAccountRequest createAcc = new CreateAccountRequest(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john.doe@mail.com",
                "0600000000",      // phoneNumber
                "1 rue de Paris",  // address
                false,             // inCouple
                1,                 // numberOfDependents
                900.0,             // monthlyIncome
                "FR7630001007941234567890185"  // IBAN
        );

        // Pour le routing correct, il faut envoyer comme message du client
        CreateAccountResponse accResponse = world.querySync(beneficiaryActor, createAcc);

        UUID beneficiaryId = accResponse.beneficiaryId();
        System.out.println("Bénéficiaire créé : " + beneficiaryId);

        // 3) Création SYNCHRONE de l’allocation → GENÈRE requestId
        CreateAllowanceRequestRequest createReq =
                new CreateAllowanceRequestRequest(beneficiaryId, "RSA");

        CreateAllowanceRequestResponse createResp =
                world.querySync(requestActor, createReq);

        UUID requestId = createResp.requestId();
        System.out.println("Request ID généré : " + requestId);

        // 4) Notification à Beneficiary → lance le traitement async
        RequestAllowanceNotification notif = new RequestAllowanceNotification(
                requestId,
                beneficiaryId,
                "RSA",
                900.0,  // monthlyIncome
                1,      // dependents
                false,  // inCouple
                true    // hasHousing
        );

        world.send(world.server().address(), beneficiaryActor, notif);
        System.out.println("Notification envoyée à Beneficiary.");

        System.out.println("\n=== TEST FULL RSA END ===");

        return ResponseEntity.ok(
                "Workflow complet lancé. beneficiaryId=" + beneficiaryId +
                " | requestId=" + requestId +
                "\nVérifie les logs server pour le traitement."
        );
    }

}
