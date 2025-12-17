package model;

/**
 * Abstract base class for all people managed by the ECMS.
 * Demonstrates Encapsulation (private fields, public getters/setters)
 * and defines the basis for Polymorphism (abstract displayDetails method).
 * * FIX: Added the public setId(String id) method to allow DAOs/Services
 * to correctly re-assign the ID during object hydration and updates.
 */
public abstract class Person {

    // --- Attributes (Encapsulation) ---
    private String id;
    private String name;
    private int age;

    // --- Constructor ---
    public Person(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    // --- Abstract Method (Polymorphism) ---
    public abstract String displayDetails();

    // --- Getters and Setters (Accessors and Mutators) ---

    public String getId() {
        return id;
    }

    /**
     * MUTATOR ADDED: Required by DAO layer to build objects from the database
     * and by the Service layer for updates.
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // --- Utility Method ---
    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Age: " + age;
    }
}