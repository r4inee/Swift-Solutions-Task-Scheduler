package swiftsolutions.interfaces.taskscheduler;


import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;
import java.util.Set;

/**
 * This interface contains functions which should be implemented by every manager in order to perform an algorithm.
 */
public interface Algorithm {
    /**
     * Method to take a set of tasks and return a valid schedule
     * @param tasks tasks that will be scheduled
     * @return the valid schedule containing the tasks
     */
    public Schedule execute(Map<Integer, Task> tasks);

    /**
     * Sets the number of processors being scheduled on
     * @param processors
     */
    public void setProcessors(int processors);
}
