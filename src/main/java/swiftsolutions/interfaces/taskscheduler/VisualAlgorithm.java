package swiftsolutions.interfaces.taskscheduler;

import swiftsolutions.interfaces.output.OutputManager;

public interface VisualAlgorithm extends Algorithm {
    void setOutputManager(OutputManager outputManager);
}
