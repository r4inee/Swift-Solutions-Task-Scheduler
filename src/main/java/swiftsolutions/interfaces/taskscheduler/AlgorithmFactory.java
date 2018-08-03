package swiftsolutions.interfaces.taskscheduler;

import swiftsolutions.taskscheduler.Algorithms;

/**
 * Factory that creates algorithm instances based off input
 */
public interface AlgorithmFactory {
    /**
     * @param algorithmName Select an algorithm from the Algorithms enum
     * @param numProcessors Select number of processors to use
     * @param numCores Select number of cores to use (if parallel)
     * @return
     */
    public Algorithm getAlgorithm(Algorithms algorithmName, int numProcessors, int numCores);
}
