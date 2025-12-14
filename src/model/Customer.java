package model;

import java.time.LocalDate;

/**
 * Represents a customer in the ECMS. Inherits common attributes from Person.
 */
public class Customer extends Person {

    private String membershipLevel; // e.g., "Bronze", "Silver", "Gold"
    private LocalDate lastPurchaseDate; // Using LocalDate for modern date handling

    // --- Constructor ---
    public Customer(String id, String name, int age, String membershipLevel, LocalDate lastPurchaseDate) {
        super(id, name, age);
        this.membershipLevel = membershipLevel;
        this.lastPurchaseDate = lastPurchaseDate;
    }

    // --- Polymorphic Implementation ---
    @Override
    public String displayDetails() {
        return String.format(
                "Customer Details: [%s], Membership: %s, Last Purchase: %s",
                super.toString(),
                membershipLevel,
                lastPurchaseDate.toString()
        );
    }

    // --- Getters and Setters Specific to Customer ---

    public String getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(String membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public LocalDate getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(LocalDate lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }
}