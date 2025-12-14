package model;

/**
 * Abstract base class for all people managed by the ECMS.
 * Demonstrates Encapsulation (private fields, public getters/setters)
 * and defines the basis for Polymorphism (abstract displayDetails method).
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
    /**
     * Forces subclasses (Employee, Customer) to implement their own way
     * of displaying details, allowing for polymorphic behavior.
     */
    public abstract String displayDetails();

    // --- Getters and Setters (Accessors and Mutators) ---
    // Note: ID only has a getter, as it should not be changeable after creation.

    public String getId() {
        return id;
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