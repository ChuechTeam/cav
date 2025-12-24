package cy.cav.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO for creating an allocataire (création d'un allocataire)
public record AllocataireDTO(
    String firstName,
    String lastName,
    LocalDate birthDate,
    String email,
    String phoneNumber,
    String address,
    boolean hasHousing,
    boolean inCouple,
    int numberOfDependents,
    BigDecimal monthlyIncome,
    String iban
) {}

