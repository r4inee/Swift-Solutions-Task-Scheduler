package swiftsolutions.unit;

import org.junit.Test;

import swiftsolutions.cli.CLIArgumentParser;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.taskscheduler.brandandboundastar.BBAAlgorithm;

import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

/**
 * This is an example of naming convention for unit test, Name with suffix Test.
 */
public class BBAtest {
    @Test
    public void testExample() {
    	DOTInputParser _inputParser = new DOTInputParser();
        BBAAlgorithm bba = new BBAAlgorithm();
        bba.setProcessors(4);
        Map<Integer, Task> tasks = null;
        try {
			tasks = _inputParser.parse("C:/Users/User/Documents/uni/306/project1/SOFTENG306_Project1/src/test/resources/test_graphs/Nodes_7_OutTree.dot");
		} catch (InputException e) {
			e.printStackTrace();
		}
        bba.execute(tasks);
        System.out.println(Arrays.deepToString(bba.getBestState()));
    }
}
