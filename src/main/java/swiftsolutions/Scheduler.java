package swiftsolutions;

import swiftsolutions.exceptions.ArgumentFormatException;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.exceptions.OutputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.interfaces.input.ArgumentParser;
import swiftsolutions.interfaces.input.InputParser;
import swiftsolutions.interfaces.output.OutputManager;
import swiftsolutions.interfaces.output.OutputWriter;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.interfaces.taskscheduler.AlgorithmFactory;
import swiftsolutions.output.AppOutputManager;
import swiftsolutions.output.DOTOutputWriter;
import swiftsolutions.output.OutputMessage;
import swiftsolutions.output.OutputType;
import swiftsolutions.cli.CLIArgumentParser;
import swiftsolutions.taskscheduler.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Winston on 7/31/2018.
 */
public class Scheduler {

    private static Scheduler _instance;

    public static Scheduler getContext() {
        if (_instance == null) {
            _instance = new Scheduler();
        }
        return _instance;
    }



    private OutputManager _outputManager;
    private ArgumentParser _argumentParser;
    private InputParser _inputParser;
    private AlgorithmFactory _algorithmFactory;
    private OutputWriter _outputWriter;


    private Scheduler() {
        _outputManager = new AppOutputManager();
        _argumentParser = new CLIArgumentParser();
        _algorithmFactory = new SchedulingAlgorithmFactory();
        _inputParser = new DOTInputParser();
        _outputWriter = new DOTOutputWriter();
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

        Map<Integer, Task> tasks;
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

        long start = System.currentTimeMillis();
        Algorithm algorithm = _algorithmFactory.getAlgorithm(Algorithms.BRANCH_AND_BOUND, numProcessors, numCores);
        Schedule outputSchedule = algorithm.execute(tasks);

        long end = System.currentTimeMillis();
        outputSchedule.convertTaskID(tasks);
        Map<Integer, Task> taskMap = convertTaskID(tasks);

        _outputManager.send(new OutputMessage(OutputType.SUCCESS,
                "Successfully ran algorithm in " + (end - start) + "ms!"));

        _outputManager.send(new OutputMessage(OutputType.DEBUG,
                "Output Graph: \n" + outputSchedule.getOutputString()));

        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Writing schedule to file..."));

        try {
            _outputWriter.serialize(_argumentParser.getFile(), outputSchedule, taskMap);
        } catch (OutputException e) {
            _outputManager.send(new OutputMessage(OutputType.DEBUG, e.getMessage()));
        }

        _outputManager.send(new OutputMessage(OutputType.SUCCESS, "Exiting program..."));
    }

    public void setOutputManager(OutputManager outputManager) {
        this._outputManager = outputManager;
    }

    private Map<Integer, Task> convertTaskID(Map<Integer, Task> tasks) {
        Map<Integer, Task> newTaskMap = new HashMap<>();
        for (Integer offsetID : tasks.keySet()) {
            newTaskMap.put(tasks.get(offsetID).getTaskID(), tasks.get(offsetID));
        }
        return newTaskMap;
    }
}
