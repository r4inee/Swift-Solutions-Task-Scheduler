package swiftsolutions.taskscheduler;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.branchandbound.BNBAlgorithm;

/**
 * Created by Winston on 8/1/2018.
 */
public enum Algorithms {
    BRANCH_AND_BOUND(new BNBAlgorithm(), "bnb-simple", false);

    private Algorithm _algorithm;
    private String _flag;
    private boolean _parallel;

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
