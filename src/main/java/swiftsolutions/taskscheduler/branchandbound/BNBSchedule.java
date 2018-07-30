package swiftsolutions.taskscheduler.branchandbound;

import swiftsolutions.taskscheduler.Schedule;

public class BNBSchedule extends Schedule {
    public BNBSchedule(Schedule parentSchedule, Schedule childSchedule) {
        super(parentSchedule, childSchedule);
    }
}
