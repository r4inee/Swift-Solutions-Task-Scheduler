package swiftsolutions.interfaces.output;

import swiftsolutions.exceptions.OutputException;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;

/**
 * An output writer that will write the output dot file.
 */
public interface OutputWriter {
    /**
     * The function that will write the output.
     * @param file the file for the output to be written into.
     * @param schedule the schedule that will be written into the output file.
     * @param offsetTaskMap the task information from the input.
     * @throws OutputException if the task information or schedule was malformed.
     */
    void serialize(String file, Schedule schedule, Map<Integer, Task> offsetTaskMap) throws OutputException;
}
