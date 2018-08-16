package swiftsolutions.output;

import swiftsolutions.util.Observable;
import swiftsolutions.util.Observer;

/**
 * Created by Winston on 7/31/2018.
 */
public class AppOutputManager implements swiftsolutions.interfaces.output.OutputManager{
    private Observable<OutputMessage> _observable;

    private boolean _consoleLog;

    public AppOutputManager() {
        this._observable = new Observable<>();
        this._consoleLog = true;

        this._observable.addObserver((obs, arg) -> {
            if (_consoleLog) {
                System.out.println(arg.getType().makeMsg(arg.getMessage()));
            }
        });
    }

    @Override
    public void send(OutputMessage message) {
        this._observable.setChanged();
        this._observable.notifyObservers(message);
    }

    @Override
    public void addObserver(Observer<OutputMessage> observer) {
        this._observable.addObserver(observer);
    }

    @Override
    public void setConsoleLog(boolean status) {
        this._consoleLog = status;
    }


}
