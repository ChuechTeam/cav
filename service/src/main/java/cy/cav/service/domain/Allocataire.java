package cy.cav.service.domain;

import java.time.LocalDate;
import java.util.UUID;

// Represents an allocataire (bénéficiaire d'allocations)

public class Allocataire {
    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
    private String phoneNumber;
    private String address;
    private boolean inCouple;              // En couple ou célibataire
    private int numberOfDependents;        // Nombre de personnes à charge (simplifié)
    private double monthlyIncome;           // Revenus mensuels
    private String iban;                   // IBAN pour les versements
    private String allocataireNumber;      // Numéro d'allocataire (généré automatiquement)
    private LocalDate registrationDate;     // Date d'inscription
    
    // Constructeur par défaut
    public Allocataire() {
        this.id = UUID.randomUUID();
    }
    
    // Constructeur avec paramètres essentiels
    public Allocataire(String firstName, String lastName, LocalDate birthDate, 
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
    
    // Getters et Setters
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
    
    public String getAllocataireNumber() {
        return allocataireNumber;
    }
    
    public void setAllocataireNumber(String allocataireNumber) {
        this.allocataireNumber = allocataireNumber;
    }
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    /**
     * Calcule le nombre total de personnes dans le foyer.
     * Calculates total number of people in household.
     */
    public int getHouseholdSize() {
        int base = inCouple ? 2 : 1;
        return base + numberOfDependents;
    }
}

