package swiftsolutions.unit;

import org.junit.Test;

import swiftsolutions.taskscheduler.brandandboundastar.BBAAlgorithm;

import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

/**
 * This is an example of naming convention for unit test, Name with suffix Test.
 */
public class bbaT {
    @Test
    public void testExample() {
        BBAAlgorithm bba = new BBAAlgorithm();
        bba.setProcessors(1);
        System.out.println(Arrays.deepToString(bba.TestExecute()));
        
    }
}
