package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * Abstract class that represents a CLI option.
 * @param <T> is the type of the argument that will be returned by the option.
 */
public abstract class CLIOption<T> {

    private String _flag;
    private int _numArgs;
    protected T _args;

    /**
     * Initialize option with flag and number of arguments expect (use 0 for dynamic). For options that will
     * have an initial value, it should be set in the overridden constructor.
     * @param flag is that flag that will be used to indicate the option
     * @param numArgs is the number of options that will be expected by the CLI (0 for dynamic)
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
     * @param args the arguments that were given following the option
     * @throws ArgumentFormatException if arguments are malformed
     */
    public abstract void verifyArgs(ArrayList<String> args) throws ArgumentFormatException;

    /**
     * Checks either correct number of arguments were input
     * @param args the arguments that were given following the option
     * @throws ArgumentFormatException if not correct number of arguments were input
     */
    public void checkNumArgs(ArrayList<String> args) throws ArgumentFormatException {
        if (this._numArgs != args.size()) {
            throw new ArgumentFormatException("Number of arguments");
        }
    }

    /**
     * Get the arguments parsed associated with the CLIOption.
     * @return arguments parsed associated with the CLIOption.
     */
    public T getArgs() {
        return this._args;
    }
}
