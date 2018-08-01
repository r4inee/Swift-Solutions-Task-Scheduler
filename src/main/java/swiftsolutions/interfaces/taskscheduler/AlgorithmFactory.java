package swiftsolutions.interfaces.taskscheduler;

import swiftsolutions.taskscheduler.Algorithms;

/**
 * Created by Winston on 8/1/2018.
 */
public interface AlgorithmFactory {
    public Algorithm getAlgorithm(Algorithms algorithmName, int numProcessors, int numCores);
}
