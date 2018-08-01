package swiftsolutions;

import swiftsolutions.exceptions.ArgumentFormatException;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.interfaces.parser.ArgumentParser;
import swiftsolutions.interfaces.parser.InputParser;
import swiftsolutions.interfaces.output.OutputManager;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.interfaces.taskscheduler.AlgorithmFactory;
import swiftsolutions.output.AppOutputManager;
import swiftsolutions.output.OutputMessage;
import swiftsolutions.output.OutputType;
import swiftsolutions.cli.CLIArgumentParser;
import swiftsolutions.taskscheduler.Algorithms;
import swiftsolutions.taskscheduler.SchedulingAlgorithmFactory;
import swiftsolutions.taskscheduler.Task;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Winston on 7/31/2018.
 */
public class Context {

    private static Context _instance;

    public static Context getContext() {
        if (_instance == null) {
            _instance = new Context();
        }
        return _instance;
    }



    private OutputManager _outputManager;
    private ArgumentParser _argumentParser;
    private InputParser _inputParser;
    private AlgorithmFactory _algorithmFactory;


    private Context() {
        _outputManager = new AppOutputManager();
        _argumentParser = new CLIArgumentParser();
        _algorithmFactory = new SchedulingAlgorithmFactory();
    }

    public OutputManager getOutputManager() {
        return this._outputManager;
    }

    public void start(String args[]) {
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Parsing arguments..."));
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Starting program..."));

        try {
            _argumentParser.parse(args);
        } catch (ArgumentFormatException e) {
            _outputManager.send(new OutputMessage(OutputType.ERROR, e.getMessage()));
            return;
        }
        DOTInputParser graphParser = new DOTInputParser();
        Set<Task> tasks = new HashSet<>();
        try {
            tasks = graphParser.parse(_argumentParser.getFile());
            _outputManager.send(new OutputMessage(OutputType.DEBUG,
                    "Parsed Graph with " + tasks.size() + " tasks"));
        } catch (InputException e) {
            _outputManager.send(new OutputMessage(OutputType.ERROR, e.getMessage()));
            return;
        }

        _outputManager.send(new OutputMessage(OutputType.SUCCESS,
                "Successfully parsed graph!"));

        int numProcessors = _argumentParser.getProcessors();
        int numCores = _argumentParser.getCoresOption().getArgs();

        Algorithm algorithm = _algorithmFactory.getAlgorithm(Algorithms.BRANCH_AND_BOUND, numProcessors, numCores);
        algorithm.execute(tasks);

    }

    public void setOutputManager(OutputManager outputManager) {
        this._outputManager = outputManager;
    }
}
