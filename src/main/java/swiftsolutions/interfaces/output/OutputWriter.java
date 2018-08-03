package swiftsolutions.interfaces.output;

import swiftsolutions.exceptions.OutputException;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.util.Set;

public interface OutputWriter {
    public void serialize(String file, Schedule schedule, Set<Task> tasks) throws OutputException;
}
