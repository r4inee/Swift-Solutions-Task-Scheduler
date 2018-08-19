package swiftsolutions.integration;

import org.junit.Test;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * Tests the task class.
 */
public class TaskTest {

    @Test
    public void testInit() {
        DOTInputParser parser = new DOTInputParser();
        Map<Integer, Task> taskMap = null;
        try {
            taskMap = parser.parse("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
        } catch (InputException e) {
            e.printStackTrace();
            fail();
        }

        Integer task0 = null;
        for (Integer task : taskMap.keySet()) {
            switch (task) {
                case 0:
                    task0 = task;
                default:
                    break;
            }
        }

        assertNotNull(task0);
        assertEquals(0, taskMap.get(task0).getTaskID());
        assertEquals(5, taskMap.get(task0).getProcessTime());
    }

    @Test
    public void testAddChildrenAndCost() {
        DOTInputParser parser = new DOTInputParser();
        Map<Integer, Task>  taskMap = null;
        try {
            taskMap = parser.parse("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
        } catch (InputException e) {
            e.printStackTrace();
            fail();
        }

        Task task5 = null;
        Task task6 = null;

        for (Integer task : taskMap.keySet()) {
            switch (task) {
                case 5:
                    task5 = taskMap.get(task);
                    break;
                case 6:
                    task6 = taskMap.get(task);
                    break;
                default:
                    break;
            }
        }
        assertNotNull(task5);
        assertNotNull(task6);
        assertEquals(0, task5.getCommunicationCosts(6));
        task5.addChild(6);
        task6.addParent(5, 1);
        assertEquals(2, task6.getNumDependency());
        assertEquals(1, task6.getCommunicationCosts(5));
    }
}
