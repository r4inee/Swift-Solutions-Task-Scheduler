package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * CLIOption to determine whether the user wants a custom output name.
 */
public class OutputOption extends CLIOption<String>{
    public OutputOption() {
        super("o", 1);
    }

    /**
     * Verifies that only 1 argument was given following -o.
     * @param args the arguments that were given following the option
     * @throws ArgumentFormatException if the arguments following -o were malformed.
     */
    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        _args = args.get(0);
    }
}
