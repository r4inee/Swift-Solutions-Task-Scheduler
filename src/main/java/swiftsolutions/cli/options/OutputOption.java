package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * Created by Winston on 7/31/2018.
 */
public class OutputOption extends CLIOption<String>{
    public OutputOption() {
        super("o", 1);
    }

    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        _args = args.get(0);
    }
}
