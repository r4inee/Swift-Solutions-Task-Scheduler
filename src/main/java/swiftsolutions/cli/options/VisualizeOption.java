package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * Created by Winston on 7/31/2018.
 */
public class VisualizeOption extends CLIOption<Boolean> {
    public VisualizeOption() {
        super("v", 0);
        this._args = false;
    }

    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        this._args = true;
    }
}
