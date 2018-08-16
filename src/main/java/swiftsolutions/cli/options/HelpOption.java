package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

public class HelpOption extends CLIOption<Boolean>{

    public HelpOption() {
        super("h", 0);
    }

    /**
     * Verifies the number of arguments is correct
     * @param args
     * @throws ArgumentFormatException
     */
    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        _args = true;
    }
}
