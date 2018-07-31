package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

public class DirectoryOption extends  CLIOption<String> {
    public DirectoryOption() {
        super("dir", 1);
    }

    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        this._args = args.get(0);
    }
}
