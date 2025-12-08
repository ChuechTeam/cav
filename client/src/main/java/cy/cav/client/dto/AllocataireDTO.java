package cy.cav.client.dto;

import java.math.*;
import java.time.LocalDate;

// DTO for creating an allocataire (création d'un allocataire)
public record AllocataireDTO(
    String firstName,
    String lastName,
    LocalDate birthDate,
    String email,
    String phoneNumber,
    String address,
    boolean inCouple,
    int numberOfDependents,
    BigDecimal monthlyIncome,
    String iban
) {}

