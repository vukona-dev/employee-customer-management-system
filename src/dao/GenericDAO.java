package dao;

import java.util.List;
import util.CustomExceptions.RecordNotFoundException; // <--- NEW IMPORT

/**
 * A generic interface defining the standard CRUD operations.
 * The use of <T> (a Type Parameter) makes this interface reusable
 * for both Employee and Customer classes (Polymorphism/Generics).
 * @param <T> The type of the entity (e.g., Employee or Customer).
 */
public interface GenericDAO<T> {

    /**
     * Finds an entity by its unique ID.
     * @param id The ID of the entity to find.
     * @return The found entity.
     * @throws RecordNotFoundException if the record is not found. // <--- NEW DOCUMENTATION
     */
    T findById(String id) throws RecordNotFoundException; // <--- MODIFIED: ADDED THROWS CLAUSE

    /**
     * Retrieves all entities of type T from the persistence store.
     * @return A list of all entities.
     */
    List<T> findAll();

    /**
     * Saves a new entity to the persistence store (CREATE operation).
     * @param entity The entity object to save.
     */
    void save(T entity);

    /**
     * Updates an existing entity in the persistence store (UPDATE operation).
     * @param entity The entity object with updated data.
     */
    void update(T entity);

    /**
     * Deletes an entity from the persistence store (DELETE operation).
     * @param id The ID of the entity to delete.
     * @return True if deletion was successful, false otherwise.
     */
    boolean delete(String id);
}