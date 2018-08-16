package swiftsolutions.interfaces.taskscheduler;

import swiftsolutions.taskscheduler.Schedule;

/**
 * Visual Algorithm that is represented as thread to be run by the GUI
 */
public abstract class VisualAlgorithm extends Thread implements Algorithm {

    /**
     * Gets the amount of branches visited
     * @return
     */
    public abstract int getBranches();

    /**
     * Gets the current bound (best schedule time)
     * @return
     */
    public abstract int getUpperbound();

    /**
     * Gets the current 'valid schedules' found (better than previous bound)
     * @return
     */
    public abstract int getValidSchedules();

    /**
     * Gets the amount of times algorithm has not run due to pruning
     * @return
     */
    public abstract int getPruned();

    /**
     * Gets the current best schedule
     * @return
     */
    public abstract int[][] getSchedule();

    /**
     * Gets the amount of processors
     * @return
     */
    public abstract int getProcessors();

    /**
     * Returns whether the algorithm is done.
     * @return
     */
    public abstract boolean isDone();

    /**
     * Gets the final schedule output
     * @return
     */
    public abstract Schedule getFinishedSchedule();
}
