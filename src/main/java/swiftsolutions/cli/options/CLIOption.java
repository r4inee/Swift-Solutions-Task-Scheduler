package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * Abstract class that represents a CLI option.
 * @param <T>
 */
public abstract class CLIOption<T> {

    private String _flag;
    private int _numArgs;
    protected T _args;

    /**
     * Initialize option with flag and number of arguments expect (use 0 for dynamic)
     * @param flag
     * @param numArgs
     */
    public CLIOption(String flag, int numArgs) {
        _flag = flag;
        _numArgs = numArgs;
    }

    public String getFlag() {
        return _flag;
    }

    /**
     * Method called by ArgumentParser that will set args
     * @param args arguments parsed by user
     * @throws ArgumentFormatException if arguments are malformed
     */
    public abstract void verifyArgs(ArrayList<String> args) throws ArgumentFormatException;

    /**
     * Checks either correct number of arguments were input
     * @param args
     * @throws ArgumentFormatException if not correct number of arguments were input
     */
    public void checkNumArgs(ArrayList<String> args) throws ArgumentFormatException {
        if (this._numArgs != args.size()) {
            throw new ArgumentFormatException("Number of arguments");
        }
    }

    /**
     * Get the arguments parsed
     * @return
     */
    public T getArgs() {
        return this._args;
    }
}
