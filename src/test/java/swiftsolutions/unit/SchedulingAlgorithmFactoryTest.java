package swiftsolutions.unit;

import org.junit.Before;
import org.junit.Test;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Algorithms;
import swiftsolutions.taskscheduler.SchedulingAlgorithmFactory;
import swiftsolutions.taskscheduler.branchandbound.BNBAlgorithm;

import static org.junit.Assert.assertTrue;

/**
 * Created by Harith on 03/08/2018.
 */
public class SchedulingAlgorithmFactoryTest {

        private SchedulingAlgorithmFactory _algoFac;

        @Before
        public void testStateInit() {
                _algoFac = new SchedulingAlgorithmFactory();
        }

        @Test
        public void testGetAlgorithm_BNB_nonParallel() {
                assertTrue(_algoFac.getAlgorithm(Algorithms.BRANCH_AND_BOUND, 2, 0) instanceof BNBAlgorithm);

        }



}
