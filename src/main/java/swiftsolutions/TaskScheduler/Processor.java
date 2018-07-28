package swiftsolutions.TaskScheduler;

import java.util.List;

/**
 * This class represent a processor object which contains the current schedule allocated by the scheduler.
 */
public class Processor {
    // Represent the current list of tasks it is performing.
    private List<Task> taskList;
    // Represent the current finish time of this processor
    private int endTime;
}
