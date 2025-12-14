package cy.cav.service;

import cy.cav.service.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for allowances, payments and anomalies
 * Uses ConcurrentHashMap for thread-safety (on peut accédee en même temps par plusieurs threads sans conflit)
 */
@Component
public class Store {
    // Stores for different entities
    private final Map<UUID, Beneficiary> beneficiaries = new ConcurrentHashMap<>();

    public Map<UUID, Beneficiary> beneficiaries() { return beneficiaries; }
}

