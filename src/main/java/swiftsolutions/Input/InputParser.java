package swiftsolutions.Input;

import swiftsolutions.TaskScheduler.Task;

import java.util.List;

public abstract class InputParser {
    private String filename;

    public InputParser(String filename) {
        this.filename = filename;
    }

    abstract public List<Task> parse(String filename);
}