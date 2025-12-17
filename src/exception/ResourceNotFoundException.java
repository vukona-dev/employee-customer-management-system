package exception;

/**
 * Exception thrown when a requested resource (Employee, Customer) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}