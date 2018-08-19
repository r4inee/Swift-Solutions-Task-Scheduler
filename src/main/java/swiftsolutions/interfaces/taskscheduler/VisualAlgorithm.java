package swiftsolutions.interfaces.taskscheduler;

import swiftsolutions.taskscheduler.Schedule;

/**
 * Visual Algorithm that is represented as thread to be run by the GUI
 */
public abstract class VisualAlgorithm extends Thread implements Algorithm {

    /**
     * @return  the amount of branches visited
     */
    public abstract int getBranches();

    /**
     * @return the current bound (best schedule time)
     */
    public abstract int getUpperbound();

    /**
     * @return the current amount 'valid schedules' found (schedule that was better than previous best schedule)
     */
    public abstract int getValidSchedules();

    /**
     * @return  the amount of times code block was been skipped due to pruning
     */
    public abstract int getPruned();

    /**
     * @return the current best schedule
     */
    public abstract int[][] getSchedule();

    /**
     * @return the amount of processors
     */
    public abstract int getProcessors();

    /**
     * @return whether the algorithm is done.
     */
    public abstract boolean isDone();

    /**
     * @return the final schedule output
     */
    public abstract Schedule getFinishedSchedule();
}
