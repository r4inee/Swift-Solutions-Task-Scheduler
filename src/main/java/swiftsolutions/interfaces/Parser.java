package swiftsolutions.interfaces;

import swiftsolutions.exceptions.InputException;
import swiftsolutions.taskscheduler.Task;

import java.util.List;

public interface Parser {

    List<Task> parse(String filename) throws Exception;
}
