package swiftsolutions.interfaces.output;

import swiftsolutions.exceptions.OutputException;
import swiftsolutions.taskscheduler.Schedule;

public interface OutputWriter {
    public void serialize(String file, Schedule schedule) throws OutputException;
}
