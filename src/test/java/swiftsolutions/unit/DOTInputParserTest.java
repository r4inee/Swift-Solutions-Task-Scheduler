package swiftsolutions.unit;

import javafx.scene.Parent;
import org.junit.Before;
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

    private DOTInputParser _parser;
    private Map<Integer, Task> _taskMap;

    /**
     * This method initializes a new DOTInputParser
     * and sets the map to be null before every test
     * is executed
     */
    @Before
    public void init(){
        _parser = new DOTInputParser();
        _taskMap = null;
    }

    /**
     * This test case test that for when no headers
     * are passed in to parse(), the graph is correctly
     * parsed. Where the nodes have the correct number of dependencies
     */
    @Test
    public void testParseNoHeaders() {
        try {
            _taskMap = _parser.parse("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
        } catch (InputException e) {
            e.printStackTrace();
            fail();
        }

        assertNotNull(_taskMap);
        assertEquals(7, _taskMap.size());

        for (Integer task : _taskMap.keySet()) {
            switch (task) {
                case 0:
                    assertEquals(0, _taskMap.get(task).getNumDependency());
                    break;
                case 1:
                    assertEquals(1, _taskMap.get(task).getNumDependency());
                    break;
                case 2:
                    assertEquals(1, _taskMap.get(task).getNumDependency());
                    break;
                case 3:
                    assertEquals(1, _taskMap.get(task).getNumDependency());
                    break;
                case 4:
                    assertEquals(1, _taskMap.get(task).getNumDependency());
                    break;
                case 5:
                    assertEquals(1, _taskMap.get(task).getNumDependency());
                    break;
                case 6:
                    assertEquals(1, _taskMap.get(task).getNumDependency());
                    break;
                default:
                    fail("Incorrectly parsed edges of the graph");
            }
        }
    }

    /**
     * Test for an invalid file path passed into parse()
     * the correct exception is thrown with the correct message
     */
    @Test
    public void testParseInvalidFile(){
        try {
            _taskMap = _parser.parse("foo");
        } catch (InputException e) {
            assertEquals("Input graph file not found",e.getMessage());
        }
    }

    /**
     * Test for a valid file that the weights are correctly parsed
     * for each node into the process time for the respective task
     */
    @Test
    public void testParseProcessTimes(){
        try {
            _taskMap = _parser.parse("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
        } catch (InputException e) {
            fail();
        }
        assertNotNull(_taskMap);
        assertEquals(7,_taskMap.size());

        for (Integer task : _taskMap.keySet()) {
            switch (task) {
                case 0:
                    assertEquals(5, _taskMap.get(task).getProcessTime());
                    break;
                case 1:
                    assertEquals(6, _taskMap.get(task).getProcessTime());
                    break;
                case 2:
                    assertEquals(5, _taskMap.get(task).getProcessTime());
                    break;
                case 3:
                    assertEquals(6, _taskMap.get(task).getProcessTime());
                    break;
                case 4:
                    assertEquals(4, _taskMap.get(task).getProcessTime());
                    break;
                case 5:
                    assertEquals(7, _taskMap.get(task).getProcessTime());
                    break;
                case 6:
                    assertEquals(7, _taskMap.get(task).getProcessTime());
                    break;
                default:
                    fail("Incorrectly parsed edges of the graph");
            }
        }
    }

    @Test
    public void testParseCommunicationTimes(){
        try {
            _taskMap = _parser.parse("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
        } catch (InputException e) {
            fail();
        }
        assertNotNull(_taskMap);
        assertEquals(7,_taskMap.size());

        for (Integer task : _taskMap.keySet()){
            switch (task){
                case 0:
                    break;
                case 1:
                    assertEquals(15,_taskMap.get(task).getCommunicationCosts(0));
                    break;
                case 2:
                    assertEquals(11,_taskMap.get(task).getCommunicationCosts(0));
                    break;
                case 3:
                    assertEquals(11,_taskMap.get(task).getCommunicationCosts(0));
                    break;
                case 4:
                    assertEquals(19,_taskMap.get(task).getCommunicationCosts(1));
                    break;
                case 5:
                    assertEquals(4,_taskMap.get(task).getCommunicationCosts(1));
                    break;
                case 6:
                    assertEquals(21,_taskMap.get(task).getCommunicationCosts(1));
                    break;
                default:
                    fail("Incorrectly parsed edges of the graph");
            }
        }
    }

}

