package swiftsolutions.taskscheduler;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.interfaces.taskscheduler.AlgorithmFactory;
import swiftsolutions.interfaces.taskscheduler.ParallelAlgorithm;

/**
 * Factory that will create an algorithm based off algorithm type, number of processors and number of cores.
 * See AlgorithmFactory.
 */
public class SchedulingAlgorithmFactory implements AlgorithmFactory {

    /**
     * @param algorithmName Select an algorithm from the Algorithms enum
     * @param numProcessors Select number of processors to use
     * @param numCores Select number of cores to use (if parallel)
     * @return the algorithm instance.
     */
    @Override
    public Algorithm getAlgorithm(Algorithms algorithmName, int numProcessors, int numCores) {
        Algorithm algorithm = algorithmName.getAlgorithm();
        if (algorithmName.isParallel()) {
            ((ParallelAlgorithm)algorithm).setCores(numCores);
        }
        algorithm.setProcessors(numProcessors);
        return algorithm;
    }
}
