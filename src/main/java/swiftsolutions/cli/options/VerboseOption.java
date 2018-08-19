package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * CLIOption that determines whether the user wants to see debug messages.
 */
public class VerboseOption extends CLIOption<Boolean> {

    public VerboseOption() {
        super("verbose", 0);
        _args = false;
    }

    /**
     * Verifies that there were no arguments following the -verbose flag.
     * @param args the arguments that were given following the option
     * @throws ArgumentFormatException if there were arguments following the -verbose flag.
     */
    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        _args = true;
    }
}
