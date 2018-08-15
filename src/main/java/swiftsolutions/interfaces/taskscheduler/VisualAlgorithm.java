package swiftsolutions.interfaces.taskscheduler;

import swiftsolutions.taskscheduler.Schedule;

public abstract class VisualAlgorithm extends Thread implements Algorithm {
    public abstract int getBranches();
    public abstract int getUpperbound();
    public abstract int getValidSchedules();
    public abstract int[][] getSchedule();
    public abstract boolean isDone();
    public abstract Schedule getFinishedSchedule();
}
