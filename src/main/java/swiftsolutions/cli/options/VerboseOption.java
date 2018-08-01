package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

public class VerboseOption extends CLIOption<Boolean>{

    public VerboseOption() {
        super("verbose", 0);
    }

    /**
     * Verifies the number of argum
     * @param args
     * @throws ArgumentFormatException
     */
    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        _args = true;
    }
}
