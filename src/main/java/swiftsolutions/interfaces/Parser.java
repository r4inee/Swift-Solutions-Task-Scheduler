package swiftsolutions.interfaces;

import swiftsolutions.exceptions.InputException;
import swiftsolutions.taskscheduler.Task;

import java.util.Set;

public interface Parser {
    Set<Task> parse() throws InputException;
}
