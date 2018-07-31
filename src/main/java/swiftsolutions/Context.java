package swiftsolutions;

import swiftsolutions.exceptions.ArgumentFormatException;
import swiftsolutions.interfaces.output.OutputManager;
import swiftsolutions.output.AppOutputManager;
import swiftsolutions.output.OutputMessage;
import swiftsolutions.output.OutputType;
import swiftsolutions.cli.ArgumentParser;

/**
 * Created by Winston on 7/31/2018.
 */
public class Context {

    private static Context _instance;

    public static Context getContext() {
        if (_instance == null) {
            _instance = new Context();
        }
        return _instance;
    }

    private OutputManager _outputManager;

    private Context() {
        _outputManager = new AppOutputManager();
    }

    public OutputManager getOutputManager() {
        return this._outputManager;
    }

    public void start(String args[]) {
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Parsing arguments..."));
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Starting program..."));
        try {
            new ArgumentParser(this).handle(args);
        } catch (ArgumentFormatException e) {
            _outputManager.send(new OutputMessage(OutputType.ERROR, e.getMessage()));
            return;
        }
    }

    public void setOutputManager(OutputManager outputManager) {
        this._outputManager = outputManager;
    }
}
