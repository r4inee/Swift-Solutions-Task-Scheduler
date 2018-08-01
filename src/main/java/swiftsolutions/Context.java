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
import swiftsolutions.taskscheduler.*;
import swiftsolutions.util.Pair;

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
        _inputParser = new DOTInputParser();
    }

    public OutputManager getOutputManager() {
        return this._outputManager;
    }

    public void start(String args[]) {
        long start = System.currentTimeMillis();
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Parsing arguments..."));
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Starting program..."));

        try {
            _argumentParser.parse(args);
        } catch (ArgumentFormatException e) {
            _outputManager.send(new OutputMessage(OutputType.ERROR, e.getMessage()));
            return;
        }

        Set<Task> tasks;
        try {
            tasks = _inputParser.parse(_argumentParser.getFile());
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

        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Executing algorithm..."));

        Algorithm algorithm = _algorithmFactory.getAlgorithm(Algorithms.BRANCH_AND_BOUND, numProcessors, numCores);
        Schedule outputSchedule = algorithm.execute(tasks);

        long end = System.currentTimeMillis();

        _outputManager.send(new OutputMessage(OutputType.SUCCESS,
                "Successfully ran algorithm in " + (end - start) + "ms!"));

        _outputManager.send(new OutputMessage(OutputType.DEBUG,
                "Output Graph: \n" + outputSchedule.getOutputString()));
    }

    public void setOutputManager(OutputManager outputManager) {
        this._outputManager = outputManager;
    }
}
