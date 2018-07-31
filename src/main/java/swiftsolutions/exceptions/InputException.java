package swiftsolutions.exceptions;

/**
 * This class is an checked exception handling for error for user input.
 * This exception is thrown when the input file cannot be found or if the file cannot be parsed correctly.
 */
public class InputException extends Exception {
    public InputException() {
        super();
    }
    public InputException(String message) {
        super(message);
    }
    public InputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InputException(Throwable cause) {
        super(cause);
    }
}
