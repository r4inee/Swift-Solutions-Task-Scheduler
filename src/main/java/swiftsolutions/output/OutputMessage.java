package swiftsolutions.output;

/**
 * Created by Winston on 7/31/2018.
 */
public class OutputMessage {
    private String _message;
    private Object _data;
    private OutputType _type;

    public OutputMessage(OutputType outputType, String message, Object data) {
        _message = message;
        _data = data;
        _type = outputType;
    }

    public OutputMessage(OutputType outputType, String message) {
        _message = message;
        _type = outputType;
    }

    public String getMessage() {
        return _message;
    }

    public Object getData() {
        return _data;
    }

    public OutputType getType() {
        return _type;
    }
}
