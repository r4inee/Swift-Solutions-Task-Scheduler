package swiftsolutions.taskscheduler;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.branchandbound.BNBAlgorithm;
import swiftsolutions.taskscheduler.brandandboundastar.BBAAlgorithm;

/**
 * Enum of algorithms that we may want to use for scheduling
 */
public enum Algorithms {
    BRANCH_AND_BOUND(new BNBAlgorithm(), "bnb-simple", false),
    BRANCH_AND_BOUND_A_STAR(new BBAAlgorithm(), "bba-simple", false);

    private Algorithm _algorithm;
    private String _flag;
    private boolean _parallel;

    /**
     * @param algorithm Algorithm instance
     * @param flag Flag used to indicate algorithm
     * @param parallel Whether the algorithm is parallel or not
     */
    Algorithms(Algorithm algorithm, String flag, boolean parallel) {
        _algorithm = algorithm;
        _flag = flag;
        _parallel = parallel;
    }

    public Algorithm getAlgorithm() {
        return _algorithm;
    }

    public String getFlag() {
        return _flag;
    }

    public boolean isParallel() {
        return _parallel;
    }
}
