package swiftsolutions.output;

public class VisualizationMessage {

    private String _message;
    private VisualizationMessageType _type;

    public VisualizationMessage(String message, VisualizationMessageType type) {
        _message = message;
        _type = type;
    }

    public String getMessage() {
        return _message;
    }

    public VisualizationMessageType getType() {
        return _type;
    }
}
