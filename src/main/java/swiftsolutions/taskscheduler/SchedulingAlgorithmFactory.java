package swiftsolutions.taskscheduler;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.interfaces.taskscheduler.AlgorithmFactory;
import swiftsolutions.interfaces.taskscheduler.ParallelAlgorithm;

/**
 * Created by Winston on 8/1/2018.
 */
public class SchedulingAlgorithmFactory implements AlgorithmFactory {
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
