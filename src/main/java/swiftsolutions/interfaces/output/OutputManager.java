package swiftsolutions.interfaces.output;

import swiftsolutions.output.OutputMessage;
import swiftsolutions.output.VisualizationMessage;
import swiftsolutions.util.Observer;

/**
 * Created by Winston on 7/31/2018.
 */
public interface OutputManager {
    public void send(OutputMessage message);
    public void addObserver(Observer<OutputMessage> observer);
    public void setConsoleLog(boolean status);
    public void sendVisual(VisualizationMessage message);
    public void addVisualObserver(Observer<VisualizationMessage> observer);
    public void notifyVisual();
}
