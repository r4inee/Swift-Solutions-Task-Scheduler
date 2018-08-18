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

    private Algorithm _algorithm;
    private Map<Integer, Task> _offsetTaskMap;
    private long _start;


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

        if (args.length == 1 && args[0].equals("-h")) {
            _outputManager.send(new OutputMessage(OutputType.HELP,
                    "java −jar scheduler.jar INPUT.dot P [OPTION]\n" +
                            "INPUT.dot\ta task graph with integer weights in dot format\n" +
                            "P        \t number of processors to schedule the INPUT graph on\n" +
                            "\n" +
                            "Optional :\n" +
                            "−p N    \tuse N cores for execution in parallel (default is sequential)\n" +
                            "−v      \tvisualise the search\n" +
                            "−o OUPUT\toutput file is named OUTPUT (default is INPUT−output.dot)\n"
            ));
            return;
        }


        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Parsing arguments..."));
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Starting program..."));

        try {
            _argumentParser.parse(args);
        } catch (ArgumentFormatException e) {
            _outputManager.send(new OutputMessage(OutputType.ERROR, e.getMessage()));
            return;
        }

        try {
            _offsetTaskMap = _inputParser.parse(_argumentParser.getFile());
            _outputManager.send(new OutputMessage(OutputType.DEBUG,
                    "Parsed Graph with " + _offsetTaskMap.size() + " tasks"));
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
        _start = System.currentTimeMillis();
       _algorithm = _argumentParser.getVisualizeOption().getArgs() ?
                _algorithmFactory.getAlgorithm(Algorithms.BRANCH_AND_BOUND_VISUAL, numProcessors, numCores) :
                _algorithmFactory.getAlgorithm(Algorithms.BRANCH_AND_BOUND_A_STAR, numProcessors, numCores);

        if (_argumentParser.getVisualizeOption().getArgs()) {
            this._outputManager.send(new OutputMessage(OutputType.STATUS, "Starting GUI..."));
            PlatformImpl.startup(() ->{
                GUI gui = new GUI();
                Stage stage = new Stage();
                String[] fileSplit = _argumentParser.getFile().split("\\\\");
                stage.setTitle("Running on input graph " + fileSplit[fileSplit.length - 1] + " with " + _argumentParser.getProcessors() + " processors");
                gui.start(stage);
                gui.setTaskMap(_offsetTaskMap);
                _algorithm.execute(_offsetTaskMap);
                gui.setAlgorithmThread((VisualAlgorithm) _algorithm);
                gui.setScheduler(this);
            });
            this._outputManager.send(new OutputMessage(OutputType.SUCCESS, "GUI Started!"));

        } else {
            executeAlgorithm();
        }
    }

    public void setOutputManager(OutputManager outputManager) {
        this._outputManager = outputManager;
    }

    public void executeAlgorithm() {
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Executing algorithm..."));

        Schedule outputSchedule = _algorithm.execute(_offsetTaskMap);


        long end = System.currentTimeMillis();
        outputSchedule.convertTaskID(_offsetTaskMap);

        _outputManager.send(new OutputMessage(OutputType.SUCCESS,
                "Successfully ran algorithm in " + (end - _start) + "ms!"));

        _outputManager.send(new OutputMessage(OutputType.DEBUG,
                "Output Graph: \n" + outputSchedule.getOutputString()));

        writeOutput(outputSchedule);

        _outputManager.send(new OutputMessage(OutputType.SUCCESS, "Exiting program..."));
    }

    public void writeOutput(Schedule schedule) {
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Writing schedule to file..."));
        try {
            if (_argumentParser.getOutputFile() != null) {
                _outputWriter.serialize(_argumentParser.getOutputFile(), schedule, _offsetTaskMap);
            } else {
                _outputWriter.serialize(_argumentParser.getFile(), schedule, _offsetTaskMap);
            }
        } catch (OutputException e) {
            _outputManager.send(new OutputMessage(OutputType.DEBUG, e.getMessage()));
        }
        this._outputManager.send(new OutputMessage(OutputType.SUCCESS, "File successfully written!"));
    }

}
