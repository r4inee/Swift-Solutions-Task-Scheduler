package swiftsolutions.interfaces.taskscheduler;

/**
 * Represents an algorithm that meant to run in parallel
 * See Algorithm
 */
public interface ParallelAlgorithm extends Algorithm {
    public void setCores(int cores);
}
