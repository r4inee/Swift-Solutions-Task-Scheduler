package swiftsolutions.exceptions;


/**
 * Exception that thrown when the arguments to the CLI are malformed.
 */
public class ArgumentFormatException extends Exception{
    public ArgumentFormatException(String message) {
        super(message);
    }
}
