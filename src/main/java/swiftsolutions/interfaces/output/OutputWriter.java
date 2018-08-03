package swiftsolutions.interfaces.output;

import swiftsolutions.exceptions.OutputException;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;

public interface OutputWriter {
    public void serialize(String file, Schedule schedule, Map<Integer, Task> taskMap) throws OutputException;
}
