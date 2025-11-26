package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

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
    private double monthlyIncome;           // Monthly income
    private String iban;                   // IBAN for payments
    private String beneficiaryNumber;      // Beneficiary number (auto-generated)
    private LocalDate registrationDate;   // Registration date
    
    // Default constructor
    public Beneficiary() {
        this.id = UUID.randomUUID();
    }
    
    // Constructor with essential parameters
    public Beneficiary(String firstName, String lastName, LocalDate birthDate, 
                      String email, boolean inCouple, int numberOfDependents) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.email = email;
        this.inCouple = inCouple;
        this.numberOfDependents = numberOfDependents;
        this.monthlyIncome = 0.0;
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
    
    public double getMonthlyIncome() {
        return monthlyIncome;
    }
    
    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }
    
    public String getIban() {
        return iban;
    }
    
    public void setIban(String iban) {
        this.iban = iban;
    }
    
    public String getBeneficiaryNumber() {
        return beneficiaryNumber;
    }
    
    public void setBeneficiaryNumber(String beneficiaryNumber) {
        this.beneficiaryNumber = beneficiaryNumber;
    }
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    /**
     * Calculates total number of people in household.
     */
    public int getHouseholdSize() {
        int base = inCouple ? 2 : 1;
        return base + numberOfDependents;
    }
}

