package swiftsolutions.cli.options;

import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

/**
 * CLIOption to determine whether the user wants to see the visualized version of the algorithm running
 * inside the GUI
 */
public class VisualizeOption extends CLIOption<Boolean> {
    public VisualizeOption() {
        super("v", 0);
        this._args = false;
    }

    /**
     * Verifies that there were no arguments following -v in the CLI
     * @param args the arguments that were given following the option
     * @throws ArgumentFormatException if there were arguments following -v in the CLI
     */
    @Override
    public void verifyArgs(ArrayList<String> args) throws ArgumentFormatException {
        super.checkNumArgs(args);
        this._args = true;
    }
}
