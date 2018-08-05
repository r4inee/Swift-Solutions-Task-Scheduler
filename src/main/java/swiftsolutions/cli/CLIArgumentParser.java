package swiftsolutions.cli;

import swiftsolutions.cli.options.*;
import swiftsolutions.exceptions.ArgumentFormatException;
import swiftsolutions.interfaces.input.ArgumentParser;

import java.util.ArrayList;


/**
 * Class that parses CLI arguments and stores options
 */
public class CLIArgumentParser implements ArgumentParser {

    private String _file;
    private String _outputFile;
    private int _processors;
    private CoresOption _coresOption;
    private VisualizeOption _visualizeOption;
    private OutputOption _outputOption;
    private DirectoryOption _directoryOption;
    private VerboseOption _verboseOption;
    private ArrayList<CLIOption> _options;

    public CLIArgumentParser() {
        // Initialize active options
        _coresOption = new CoresOption();
        _visualizeOption = new VisualizeOption();
        _directoryOption = new DirectoryOption();
        _verboseOption = new VerboseOption();
        _outputOption = new OutputOption();

        _options = new ArrayList<>();
        _options.add(_coresOption);
        _options.add(_visualizeOption);
        _options.add(_outputOption);
        _options.add(_directoryOption);
        _options.add(_verboseOption);
    }

    // Parse arguments
    public void parse(String[] args) throws ArgumentFormatException {
        if (args.length < 2) {
            throw new ArgumentFormatException(
                    "Less arguments than expected, try using -h for help.");
        }

        // Get graph file to schedule
        _file = args[0];

        // Get number of processors to schedule on
        _processors = parseInt(args[1],
                "Second argument must be an integer, try using -h for help.");

        // If there are no more arguments (0 options used we can stop)
        if (args.length < 3) {
            return;
        }

        // If the first argument after the 2, is not a flag, it is invalid.
        if (args[2].charAt(0) != '-') {
            throw new ArgumentFormatException("Invalid first flag, try using -h for help.");
        }

        // Collect the arguments for each flag and then lookup the flag.
        for (int i = 2; i < args.length; i++) {
            String flag = args[i].substring(1);
            ArrayList<String> optionArgs = new ArrayList<>();
            while(i < args.length - 1 && args[i + 1].charAt(0) != '-') {
                optionArgs.add(args[i + 1]);
                i++;
            }
            this.insertOption(flag, optionArgs);
        }
    }

    /**
     * Function used for attempting parse integer from string
     * @param num string of number to be parsed
     * @param errMsg error message to show if parse fails
     * @return number that parsed
     * @throws ArgumentFormatException
     */
    private int parseInt(String num, String errMsg) throws ArgumentFormatException{
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(
                    errMsg);
        }
    }

    /**
     * Check option flag against active options and set the arguments
     * @param flag
     * @param args
     * @throws ArgumentFormatException
     */
    private void insertOption(String flag, ArrayList<String> args) throws ArgumentFormatException{
        for (CLIOption option : _options) {
            if (option.getFlag().equals(flag)) {
                option.verifyArgs(args);
                if (option.getFlag().equals("o")) {
                    _outputFile = (String)option.getArgs();
                }
            }
        }
    }

    /**
     * Get the graph file to be scheduled
     * @return
     */
    public String getFile() {
        return _file;
    }

    public String getOutputFile() {
        return _outputFile;
    }

    /**
     * Get number of processors to schedule
     * @return
     */
    public int getProcessors() {
        return _processors;
    }

    /**
     * If parallel, get number of cores to use
     * @return
     */
    public CoresOption getCoresOption() {
        return _coresOption;
    }

    /**
     * Check whether the client wants to visualize the scheduling process
     * @return
     */
    public VisualizeOption getVisualizeOption() {
        return _visualizeOption;
    }

    /**
     * Check whether the client wants to customize the output location
     * @return
     */
    public OutputOption getOutputOption() {
        return _outputOption;
    }

    /**
     * Check whether the client wants debug messages
     * @return
     */
    public VerboseOption getVerboseOption() {
        return _verboseOption;
    }
}
