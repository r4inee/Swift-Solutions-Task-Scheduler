package swiftsolutions.cli;

import swiftsolutions.cli.options.*;
import swiftsolutions.exceptions.ArgumentFormatException;
import swiftsolutions.interfaces.input.ArgumentParser;

import java.util.ArrayList;


/**
 * Created by Winston on 7/31/2018.
 */
public class CLIArgumentParser implements ArgumentParser {

    private String _file;
    private int _processors;
    private CoresOption _coresOption;
    private VisualizeOption _visualizeOption;
    private OutputOption _outputOption;
    private DirectoryOption _directoryOption;
    private VerboseOption _verboseOption;
    private ArrayList<CLIOption> _options;

    public CLIArgumentParser() {
        _coresOption = new CoresOption();
        _visualizeOption = new VisualizeOption();
        _outputOption = new OutputOption();
        _directoryOption = new DirectoryOption();
        _verboseOption = new VerboseOption();

        _options = new ArrayList<>();
        _options.add(_coresOption);
        _options.add(_visualizeOption);
        _options.add(_outputOption);
        _options.add(_directoryOption);
        _options.add(_verboseOption);
    }

    public void parse(String[] args) throws ArgumentFormatException {
        if (args.length < 2) {
            throw new ArgumentFormatException(
                    "Less arguments than expected, try using -h for help.");
        }

        _file = args[0];
        _processors = parseInt(args[1],
                "Second argument must be an integer, try using -h for help.");

        if (args.length < 3) {
            return;
        }

        if (args[2].charAt(0) != '-') {
            throw new ArgumentFormatException("Invalid first flag, try using -h for help.");
        }

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

    private int parseInt(String num, String errMsg) throws ArgumentFormatException{
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            throw new ArgumentFormatException(
                    errMsg);
        }
    }

    private void insertOption(String flag, ArrayList<String> args) throws ArgumentFormatException{
        for (CLIOption option : _options) {
            if (option.getFlag().equals(flag)) {
                option.verifyArgs(args);
            }
        }
    }


    public String getFile() {
        return _file;
    }

    public int getProcessors() {
        return _processors;
    }

    public CoresOption getCoresOption() {
        return _coresOption;
    }

    public VisualizeOption getVisualizeOption() {
        return _visualizeOption;
    }

    public OutputOption getOutputOption() {
        return _outputOption;
    }
}
