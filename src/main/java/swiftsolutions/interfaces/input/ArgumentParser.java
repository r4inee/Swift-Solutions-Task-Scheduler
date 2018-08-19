package swiftsolutions.interfaces.input;

import swiftsolutions.cli.options.*;
import swiftsolutions.exceptions.ArgumentFormatException;

/**
 * An argument parser that for the Scheduling application, it allows the user to get the various input flags and their
 * corresponding arguments that were parsed in the by the user.
 */
public interface ArgumentParser  {

    /**
     * Will parse the arguments
     * @param args the arguments that were passed in the by user, should be processed will String.split(' '), before
     *             being passed to the ArgumentParser
     * @throws ArgumentFormatException if the arguments are malformed
     */
    void parse(String[] args) throws ArgumentFormatException;

    /**
     * @return the path of the graph file to be scheduled.
     */
    String getFile();

    /**
     * @return the string name of the preferred output file the default is [INPUT]-output.dot
     */
    String getOutputFile();

    /**
     * @return the CLIOption that holds the number of processors to schedule.
     */
    int getProcessors();

    /**
     * @return the CLIOption whether the algorithm will run in parallel mode or sequential mode, if there are 0 cores
     * the algorithm will run in sequential mode (the default option), if there is more than 1 core specified, the
     * algorithm will run in parallel mode with the amount of cores specified.
     */
    CoresOption getCoresOption();

    /**
     * @return the CLIOption that contains whether the user wants to visualize the algorithm as it runs.
     */
    VisualizeOption getVisualizeOption();

    /**
     * @return the CLIOption that contains whether the client wants to customize the output location.
     */
    OutputOption getOutputOption();

    /**
     * @return the CLIOption that contains whether the user would like to display the help message (will not run the
     * algorithm).
     */
    HelpOption getHelpOption();

    /**
     * @return the CLIOption that contains whether the user would like to see DEBUG messages during the run.
     */
    VerboseOption getVerboseOption();

}
