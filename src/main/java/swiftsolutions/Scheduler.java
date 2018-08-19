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
 * Class that will coordinate the various components such that they will work together in a way to produce a valid,
 * optimal schedule based off the input arguments. Various types of scheduling can be set, this is done in the input
 * arguments that are parsed into the start() function.
 */
public class Scheduler {

    private static Scheduler _instance;

    public static Scheduler getInstance() {
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

    /**
     * @return the output manager used by the scheduler.
     */
    public OutputManager getOutputManager() {
        return this._outputManager;
    }

    /**
     * Starts the scheduling
     * @param args the input arguments parsed by the user.
     */
    public void start(String args[]) {

        // Get the current system time to calculate performance.
        _start = System.currentTimeMillis();

        // Check if the user is asking for help on how to use the application.
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

        // Try to parse the arguments.
        try {
            _argumentParser.parse(args);
        } catch (ArgumentFormatException e) {
            _outputManager.send(new OutputMessage(OutputType.ERROR, e.getMessage()));
            return;
        }

        // Check if the user wants to see debug messages.
        if (_argumentParser.getVerboseOption().getArgs()) {
            _outputManager.setVerbose(true);
        }

        // Parse the input file.
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

        // Create an algorithm instance for the application to run.
        int numProcessors = _argumentParser.getProcessors();
        int numCores = _argumentParser.getCoresOption().getArgs();

        if (numCores != 0) {
            if (_argumentParser.getVisualizeOption().getArgs()) {
                this._outputManager.send(new OutputMessage(OutputType.DEBUG,
                        "Using parallel algorithm with  visualization."));
                _algorithm =_algorithmFactory
                        .getAlgorithm(Algorithms.BRANCH_AND_BOUND_A_PARALLEL_VISUAL, numProcessors, numCores);
            } else {
                this._outputManager.send(new OutputMessage(OutputType.DEBUG,
                        "Using parallel algorithm."));
                _algorithm = _algorithmFactory
                        .getAlgorithm(Algorithms.BRANCH_AND_BOUND_A_STAR_PARALLEL, numProcessors, numCores);
            }
        } else {
            if (_argumentParser.getVisualizeOption().getArgs()) {
                this._outputManager.send(new OutputMessage(OutputType.DEBUG,
                        "Using sequential algorithm with visualization."));
                _algorithm = _algorithmFactory
                        .getAlgorithm(Algorithms.BRANCH_AND_BOUND_A_STAR_VISUAL, numProcessors, numCores);
            } else {
                this._outputManager.send(new OutputMessage(OutputType.DEBUG,
                        "Using sequential algorithm."));
                _algorithm = _algorithmFactory
                        .getAlgorithm(Algorithms.BRANCH_AND_BOUND_A_STAR, numProcessors, numCores);
            }
        }

        // Check if the user wants a visualized run of the algorithm.
        if (_argumentParser.getVisualizeOption().getArgs()) {
            this._outputManager.send(new OutputMessage(OutputType.STATUS, "Starting GUI..."));
            // Start the GUI.
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
            // If the user does not want a visualized version of the algoritm, execute the algorithm.
            executeAlgorithm();
        }
    }

    /**
     * Sets the output manager used by the application.
     * @param outputManager output manager used by the application.
     */
    public void setOutputManager(OutputManager outputManager) {
        this._outputManager = outputManager;
    }

    /**
     * Executes the algorithm.
     */
    public void executeAlgorithm() {
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Executing algorithm..."));

        // Execute the algorithm.
        Schedule outputSchedule = _algorithm.execute(_offsetTaskMap);


        // Calculates run time.
        long end = System.currentTimeMillis();
        outputSchedule.convertTaskID(_offsetTaskMap);

        _outputManager.send(new OutputMessage(OutputType.SUCCESS,
                "Successfully ran algorithm in " + (end - _start) + "ms!"));

        _outputManager.send(new OutputMessage(OutputType.DEBUG,
                "Output Graph: \n" + outputSchedule.getOutputString()));

        // Write the output file.
        writeOutput(outputSchedule);

        _outputManager.send(new OutputMessage(OutputType.SUCCESS, "Exiting program..."));
    }

    /**
     * Write the output file.
     * @param schedule output schedule to be written to the output file.
     */
    public void writeOutput(Schedule schedule) {
        this._outputManager.send(new OutputMessage(OutputType.STATUS, "Writing schedule to file..."));

        // Write the output file.
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
