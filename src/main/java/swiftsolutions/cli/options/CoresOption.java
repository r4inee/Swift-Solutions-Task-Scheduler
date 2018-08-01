package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * Created by Winston on 7/31/2018.
 */
public class CoresOption extends CLIOption<Integer> {

    public CoresOption() {
        super("p", 1);
        _args = 1;
    }

    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        try {
            int cores =Integer.parseInt(args.get(0));
            if (cores < 1) {
                throw new ArgumentFormatException(
                        "Value after processor flag (-v) must be a positive integer!");
            }
            super._args = cores;
        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(
                    "Value after processor flag (-v) must be a valid integer!");
        }
    }
}
