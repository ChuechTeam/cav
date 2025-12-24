package cy.cav.service.config;

import cy.cav.framework.*;
import cy.cav.service.actors.Prefecture;
import cy.cav.service.Store;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PrefectureService {
    private final World world;
    private final DefaultBeneficiaries defaultBeneficiaries;

    // C'est ici notre "Annuaire" : on associe un Nom (String) à une Adresse (ActorAddress)
    private final Map<String, ActorAddress> registry = new HashMap<>();

    // Injection des dépendances nécessaires via le constructeur
    public PrefectureService(World world, DefaultBeneficiaries defaultBeneficiaries) {
        this.world = world;
        this.defaultBeneficiaries = defaultBeneficiaries;
    }

    /**
     * Cette méthode est appelée automatiquement quand l'application démarre.
     * C'est ici qu'on crée nos préfectures et qu'on remplit l'annuaire.
     */
    @PostConstruct
    public void init() {
        createPrefecture("Paris");
        createPrefecture("Lyon");
        createPrefecture("Marseille");
    }

    private void createPrefecture(String name) {
        // On spawn l'acteur Prefecture.
        // On passe 'null' pour le Store et ServerFinder car on n'en a pas besoin pour l'instant
        ActorAddress addr = world.spawn(init -> new Prefecture(
                init,
                null,
                defaultBeneficiaries,
                null
        ));

        // On enregistre l'adresse dans notre Map en minuscule pour éviter les soucis de majuscules
        registry.put(name.toLowerCase(), addr);
        System.out.println("Préfecture enregistrée : " + name + " -> " + addr);
    }

    /**
     * Permet de récupérer l'adresse d'un acteur à partir du nom de la ville.
     */
    public ActorAddress getAddress(String name) {
        return registry.get(name.toLowerCase());
    }

    /**
     * Permet de récupérer la liste de toutes les préfectures disponibles.
     */
    public Set<String> getAvailablePrefectures() {
        return registry.keySet();
    }
}