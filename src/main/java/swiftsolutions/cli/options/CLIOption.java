package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * Created by Winston on 7/31/2018.
 */
public abstract class CLIOption<T> {

    private String _flag;
    private int _numArgs;
    protected T _args;

    public CLIOption(String flag, int numArgs) {
        _flag = flag;
        _numArgs = numArgs;
    }

    public String getFlag() {
        return _flag;
    }

    public abstract void verifyArgs(ArrayList<String> args) throws ArgumentFormatException;

    public void checkNumArgs(ArrayList<String> args) throws ArgumentFormatException {
        if (this._numArgs != args.size()) {
            throw new ArgumentFormatException("Number of arguments");
        }
    }

    public T getArgs() {
        return this._args;
    }
}
