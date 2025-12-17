package util;

/**
 * A container for custom exceptions used throughout the application.
 */
public class CustomExceptions {

    /**
     * Exception thrown when a requested record is not found in the persistence store.
     * This must be public and static to be referenced easily by DAOs.
     */
    public static class RecordNotFoundException extends Exception {

        public RecordNotFoundException(String message) {
            super(message);
        }

        public RecordNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Add other custom exceptions here later (e.g., InvalidDataException)
}