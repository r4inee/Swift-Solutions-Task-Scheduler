package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * CLIOption that will tell the program whether or not to show the help messages.
 */
public class HelpOption extends CLIOption<Boolean>{

    public HelpOption() {
        super("h", 0);
        _args = false;
    }

    /**
     * See CLIOption#verifyArgs, verifies that there were no arguments after -h
     * @param args arguments passed in following the -h flag (should be none)
     * @throws ArgumentFormatException if there were arguments following -h
     */
    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        _args = true;
    }

}

