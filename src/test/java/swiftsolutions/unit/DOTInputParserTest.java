package swiftsolutions.unit;

import org.junit.BeforeClass;
import org.junit.Test;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;


/**
 * This contains basic unit tests for testing parsing of graph data from different input formats.
 */

public class DOTInputParserTest {

    @Test
    public void testParseNoHeaders() {
        DOTInputParser parser = new DOTInputParser();
        Map<Integer, Task> taskMap = null;
        try {
            taskMap = parser.parse("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
        } catch (InputException e) {
            e.printStackTrace();
            fail();
        }

        assertNotNull(taskMap);
        assertEquals(7, taskMap.size());

        for (Integer task : taskMap.keySet()) {
            switch (task) {
                case 0:
                    assertEquals(0, taskMap.get(task).getNumDependency());
                    break;
                case 1:
                    assertEquals(1, taskMap.get(task).getNumDependency());
                    break;
                case 2:
                    assertEquals(1, taskMap.get(task).getNumDependency());
                    break;
                case 3:
                    assertEquals(1, taskMap.get(task).getNumDependency());
                    break;
                case 4:
                    assertEquals(1, taskMap.get(task).getNumDependency());
                    break;
                case 5:
                    assertEquals(1, taskMap.get(task).getNumDependency());
                    break;
                case 6:
                    assertEquals(1, taskMap.get(task).getNumDependency());
                    break;
                default:
                    fail("Incorrectly parsed edges of the graph");
            }
        }
    }
}

