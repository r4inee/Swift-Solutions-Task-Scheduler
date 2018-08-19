package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * CLIOption that will determine whether the program will run in parallel, or sequential mode.
 */
public class CoresOption extends CLIOption<Integer> {

    public CoresOption() {
        super("p", 1);
        // The default number of cores is 0, indicating that the program will run sequentially.
        _args = 0;
    }

    /**
     * See CLIOption#verifyArgs
     * @param args the arguments that were given following the option
     * @throws ArgumentFormatException
     *          if an invalid number of arguments, or malformed arguments will given the as arguments.
     */
    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        try {
            int cores =Integer.parseInt(args.get(0));
            if (cores < 1) {
                throw new ArgumentFormatException(
                        "Value after processor flag (-p) must be a positive integer!");
            }
            super._args = cores;
        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(
                    "Value after processor flag (-p) must be a valid integer!");
        }
    }
}
