package dao;

import model.User;
// Import the wrapper class that contains the exception
import util.CustomExceptions;

/**
 * Extends GenericDAO to provide specific data access methods for the User entity.
 */
public interface UserDAO extends GenericDAO<User> {

    /**
     * Retrieves a User object based on the provided unique username.
     * @param username The username string to look up.
     * @return The User object.
     * @throws CustomExceptions.RecordNotFoundException if no user is found.
     */
    User findByUsername(String username) throws CustomExceptions.RecordNotFoundException;
}