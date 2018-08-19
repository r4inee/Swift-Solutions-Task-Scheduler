package swiftsolutions.interfaces.taskscheduler;

/**
 * Represents an algorithm that meant to run in parallel
 * See Algorithm
 */
public interface ParallelAlgorithm extends Algorithm {
    /**
     * @param cores the amount of threads to be created by the parallel algorithm
     */
    void setCores(int cores);
}
