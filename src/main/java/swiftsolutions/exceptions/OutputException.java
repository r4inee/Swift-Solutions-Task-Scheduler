package swiftsolutions.exceptions;

/**
 * This class is an checked that the output file has been correctly written.
 */
public class OutputException extends Exception {
    public OutputException(String message) {
        super(message);
    }
}
