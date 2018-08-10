package swiftsolutions.taskscheduler.brandandboundastar;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;
import java.util.Queue;

public class BBAAlgorithm implements Algorithm{

    private int _numProcessors;
    private Schedule _schedule;

    /**
     * Overrides Algorithm execute
     * See Algorithm#execute()
     * @param tasks tasks that will be scheduled
     * @return
     */
    @Override
    public Schedule execute(Map<Integer, Task> tasks) {
        return null;
    }

    /**
     * Overrides Algorithm setProcessors
     * See Algorithm#setProcessors()
     * @param processors
     */
    @Override
    public void setProcessors(int processors) {
        _numProcessors = processors;
    }
}
