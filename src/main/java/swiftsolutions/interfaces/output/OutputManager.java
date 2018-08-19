package swiftsolutions.interfaces.output;

import swiftsolutions.output.OutputMessage;
import swiftsolutions.util.Observer;

/**
 * OutputManager for the application will manage messages to the user.
 */
public interface OutputManager {
    /**
     * Send a message to the output
     * @param message an output message that will be sent
     */
     void send(OutputMessage message);

    /**
     * Add an observer to the output messages.
     * @param observer
     */
    void addObserver(Observer<OutputMessage> observer);

    /**
     * @param status is the variable that indicates whether we want to console log any output messages.
     */
     void setConsoleLog(boolean status);

    /**
     * @param status is the variable that indicates whether we want to see debug messages.
     */
    void setVerbose(boolean status);

    /**
     * @param status is the variable that indicates whether we want to see colored messages.
     */
    void setColor(boolean status);
}
