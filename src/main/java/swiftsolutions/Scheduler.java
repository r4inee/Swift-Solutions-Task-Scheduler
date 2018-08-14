package swiftsolutions;

import com.sun.javafx.application.PlatformImpl;
import javafx.stage.Stage;
import swiftsolutions.exceptions.ArgumentFormatException;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.exceptions.OutputException;
import swiftsolutions.gui.GUI;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.interfaces.input.ArgumentParser;
import swiftsolutions.interfaces.input.InputParser;
import swiftsolutions.interfaces.output.OutputManager;
import swiftsolutions.interfaces.output.OutputWriter;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.interfaces.taskscheduler.AlgorithmFactory;
import swiftsolutions.interfaces.taskscheduler.VisualAlgorithm;
import swiftsolutions.output.AppOutputManager;
import swiftsolutions.output.DOTOutputWriter;
import swiftsolutions.output.OutputMessage;
import swiftsolutions.output.OutputType;
import swiftsolutions.cli.CLIArgumentParser;
import swiftsolutions.taskscheduler.*;

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

        Map<Integer, Task> offsetTaskMap;
        try {
            offsetTaskMap = _inputParser.parse(_argumentParser.getFile());
            _outputManager.send(new OutputMessage(OutputType.DEBUG,
                    "Parsed Graph with " + offsetTaskMap.size() + " tasks"));
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

        Algorithm algorithm = _algorithmFactory.getAlgorithm(Algorithms.BRANCH_AND_BOUND_A_STAR, numProcessors, numCores);

        if (_argumentParser.getVisualizeOption().getArgs()) {
            PlatformImpl.startup(() ->{
                GUI gui = new GUI();
                gui.start(new Stage());
                gui.setOutputManager(_outputManager);
            });

            algorithm = _algorithmFactory.getAlgorithm(Algorithms.BRANCH_AND_BOUND_VISUAL, numProcessors, numCores);
            ((VisualAlgorithm)algorithm).setOutputManager(_outputManager);
        }

        Schedule outputSchedule = algorithm.execute(offsetTaskMap);

        long end = System.currentTimeMillis();
        outputSchedule.convertTaskID(offsetTaskMap);

        _outputManager.send(new OutputMessage(OutputType.SUCCESS,
                "Successfully ran algorithm in " + (end - start) + "ms!"));

        _outputManager.send(new OutputMessage(OutputType.DEBUG,
                "Output Graph: \n" + outputSchedule.getOutputString()));

        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Writing schedule to file..."));

        try {
            if (_argumentParser.getOutputFile() != null) {
                _outputWriter.serialize(_argumentParser.getOutputFile(), outputSchedule, offsetTaskMap);
            } else {
                _outputWriter.serialize(_argumentParser.getFile(), outputSchedule, offsetTaskMap);
            }
        } catch (OutputException e) {
            _outputManager.send(new OutputMessage(OutputType.DEBUG, e.getMessage()));
        }

        _outputManager.send(new OutputMessage(OutputType.SUCCESS, "Exiting program..."));
    }

    public void setOutputManager(OutputManager outputManager) {
        this._outputManager = outputManager;
    }

}
