package swiftsolutions.interfaces.input;

import swiftsolutions.exceptions.InputException;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;

public interface InputParser {
    public Map<Integer, Task> parse(String filename) throws InputException;
}
