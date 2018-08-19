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
    private HelpOption _helpOption;
    private VerboseOption _verboseOption;
    private ArrayList<CLIOption> _options;

    public CLIArgumentParser() {
        // Initialize active options
        _coresOption = new CoresOption();
        _visualizeOption = new VisualizeOption();
        _helpOption = new HelpOption();
        _outputOption = new OutputOption();
        _verboseOption = new VerboseOption();

        _options = new ArrayList<>();
        _options.add(_coresOption);
        _options.add(_visualizeOption);
        _options.add(_outputOption);
        _options.add(_helpOption);
        _options.add(_verboseOption);
    }

    /**
     * Method that will passed the arguments parsed in by the user from the CLI
     * @param args the arguments that were passed in by the user from the CLI
     * @throws ArgumentFormatException if the arguments were malformed.
     */
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
     * Function used for attempting parse integer from string.
     * @param num string of number to be parsed
     * @param errMsg error message to show if parse fails
     * @return number that parsed
     * @throws ArgumentFormatException if the integer that was parsed was malformed.
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
     * Check option flag against active options and set the arguments.
     * @param flag
     * @param args
     * @throws ArgumentFormatException if the arguments following the flag were determined to be malformed.
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
     * @return the path of the graph file to be scheduled.
     */
    public String getFile() {
        return _file;
    }

    /**
     * @return the string name of the preferred output file the default is [INPUT]-output.dot
     */
    @Override
    public String getOutputFile() {
        return _outputFile;
    }

    /**
     * @return the CLIOption that holds the number of processors to schedule.
     */
    @Override
    public int getProcessors() {
        return _processors;
    }

    /**
     * @return the CLIOption whether the algorithm will run in parallel mode or sequential mode, if there are 0 cores
     * the algorithm will run in sequential mode (the default option), if there is more than 1 core specified, the
     * algorithm will run in parallel mode with the amount of cores specified.
     */
    @Override
    public CoresOption getCoresOption() {
        return _coresOption;
    }

    /**
     * @return the CLIOption that contains whether the user wants to visualize the algorithm as it runs.
     */
    @Override
    public VisualizeOption getVisualizeOption() {
        return _visualizeOption;
    }

    /**
     * @return the CLIOption that contains whether the client wants to customize the output location.
     */
    @Override
    public OutputOption getOutputOption() {
        return _outputOption;
    }

    /**
     * @return the CLIOption that contains whether the user would like to display the help message (will not run the
     * algorithm).
     */
    @Override
    public HelpOption getHelpOption() {
        return _helpOption;
    }

    /**
     * @return the CLIOption that contains whether the user would like to see DEBUG messages during the run.
     */
    @Override
    public VerboseOption getVerboseOption() {
        return _verboseOption;
    }
}

