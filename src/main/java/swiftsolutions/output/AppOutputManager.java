package swiftsolutions.output;

import swiftsolutions.interfaces.output.OutputManager;
import swiftsolutions.util.Observable;
import swiftsolutions.util.Observer;

/**
 * The OutputManager implementation for the application. See OutputManager.
 */
public class AppOutputManager implements OutputManager {
    private Observable<OutputMessage> _observable;

    private boolean _consoleLog;
    private boolean _verbose;

    private boolean _color;

    public AppOutputManager() {
        this._observable = new Observable<>();
        this._consoleLog = true;
        this._verbose = false;

        this._color = false;


        // Observer for console logging messages.
        this._observable.addObserver((obs, arg) -> {
            if (_consoleLog) {
                if (arg.getType() == OutputType.DEBUG && !_verbose) {
                    return;
                }

                String msg = _color ? arg.getType().makeMsg(arg.getMessage()) :
                        arg.getType().makeNoColorMsg(arg.getMessage());
                System.out.println(msg);

            }
        });
    }

    /**
     * Send a message to the output
     * @param message an output message that will be sent
     */
    @Override
    public void send(OutputMessage message) {
        this._observable.setChanged();
        this._observable.notifyObservers(message);
    }

    /**
     * Add an observer to the output messages.
     * @param observer
     */
    @Override
    public void addObserver(Observer<OutputMessage> observer) {
        this._observable.addObserver(observer);
    }

    /**
     * @param status is the variable that indicates whether we want to console log any output messages.
     */
    @Override
    public void setConsoleLog(boolean status) {
        this._consoleLog = status;
    }

    /**
     * @param status is the variable that indicates whether we want to see debug messages.
     */
    @Override
    public void setVerbose(boolean status) {
        _verbose = status;
    }

    @Override
    public void setColor(boolean status) {
        _color = status;
    }

}
