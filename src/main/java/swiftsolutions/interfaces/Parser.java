package swiftsolutions.interfaces;

import swiftsolutions.taskscheduler.Task;

import java.util.Set;

public interface Parser {

    Set<Task> parse(String filename) throws Exception;
}
