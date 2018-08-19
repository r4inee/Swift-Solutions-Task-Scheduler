package swiftsolutions.output;

/**
 * Represents an output message that will be used by a OutputManager. See OutputManager.
 */
public class OutputMessage {
    private String _message;
    private OutputType _type;


    public OutputMessage(OutputType outputType, String message) {
        _message = message;
        _type = outputType;
    }

    /**
     * @return the string message to be displayed.
     */
    public String getMessage() {
        return _message;
    }

    /**
     * @return the type of this message. See OutputType
     */
    public OutputType getType() {
        return _type;
    }
}
