package swiftsolutions.interfaces.taskscheduler;


import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.util.Set;

/**
 * This interface contains functions which should be implemented by every manager in order to perform an algorithm.
 */
public interface Algorithm {
    public Schedule execute(Set<Task> tasks);
    public void setProcessors(int processors);
}
