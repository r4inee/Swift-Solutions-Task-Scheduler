package swiftsolutions.interfaces.input;

import swiftsolutions.exceptions.InputException;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;

/**
 * An input parser of the application that will parse a DOT file into a Map of Task ID to Task information.
 */
public interface InputParser {

    /**
     * This is a method which check if the file exist in the location supplied in the constructor or setter.
     * @return A set of object representing each of the task needed to be scheduled.
     * @throws InputException if the input DOT file was malformed.
     */
     Map<Integer, Task> parse(String filename) throws InputException;
}
