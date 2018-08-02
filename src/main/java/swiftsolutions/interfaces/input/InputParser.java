package swiftsolutions.interfaces.input;

import swiftsolutions.exceptions.InputException;
import swiftsolutions.taskscheduler.Task;

import java.util.Set;

public interface InputParser {
    public Set<Task> parse(String filename) throws InputException;
}
