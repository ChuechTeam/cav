package cy.cav.service.domain;

import cy.cav.protocol.*;

import java.math.*;
import java.time.*;
import java.util.*;

/**
 * Represents a beneficiary (allocataire) of allowances.
 */
public class Beneficiary {
    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
    private String phoneNumber;
    private String address;
    private boolean inCouple;              // In couple or single
    private int numberOfDependents;        // Number of dependents
    private BigDecimal monthlyIncome;           // Monthly income
    private String iban;                   // IBAN for payments
    private LocalDate registrationDate;   // Registration date
    private final List<Payment> payments = new ArrayList<>();

    public Beneficiary(String firstName, String lastName, LocalDate birthDate, String email, String phoneNumber, String address, boolean inCouple, int numberOfDependents, BigDecimal monthlyIncome, String iban, LocalDate registrationDate) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.inCouple = inCouple;
        this.numberOfDependents = numberOfDependents;
        this.monthlyIncome = monthlyIncome;
        this.iban = iban;
        this.registrationDate = registrationDate;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isInCouple() {
        return inCouple;
    }

    public void setInCouple(boolean inCouple) {
        this.inCouple = inCouple;
    }

    public int getNumberOfDependents() {
        return numberOfDependents;
    }

    public void setNumberOfDependents(int numberOfDependents) {
        this.numberOfDependents = numberOfDependents;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public BeneficiaryProfile toProfile() {
        return new BeneficiaryProfile(
                firstName,
                lastName,
                birthDate,
                email,
                phoneNumber,
                address,
                inCouple,
                numberOfDependents,
                monthlyIncome,
                iban,
                registrationDate);
    }

    /**
     * Calculates total number of people in household.
     */
    public int getHouseholdSize() {
        int base = inCouple ? 2 : 1;
        return base + numberOfDependents;
    }
}

