package swiftsolutions.unit;

import org.junit.Test;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.taskscheduler.Task;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

public class TaskTest {

    @Test
    public void testInit() {
        DOTInputParser parser = new DOTInputParser("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
        Set<Task> allTasks = null;
        try {
            allTasks = parser.parse();
        } catch (InputException e) {
            e.printStackTrace();
            fail();
        }

        Task task0 = null;
        for (Task task : allTasks) {
            switch (task.getTaskID()) {
                case 0:
                    task0 = task;
                default:
                    break;
            }
        }

        assertNotNull(task0);
        assertEquals(0, task0.getTaskID());
        assertEquals(5, task0.getProcessTime());
    }

    @Test
    public void testAddChildrenAndCost() {
        DOTInputParser parser = new DOTInputParser("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
        Set<Task> allTasks = null;
        try {
            allTasks = parser.parse();
        } catch (InputException e) {
            e.printStackTrace();
            fail();
        }

        Task task5 = null;
        Task task6 = null;

        for (Task task : allTasks) {
            switch (task.getTaskID()) {
                case 5:
                    task5 = task;
                    break;
                case 6:
                    task6 = task;
                    break;
                default:
                    break;
            }
        }
        assertNotNull(task5);
        assertNotNull(task6);
        assertEquals(0, task6.getCommunicationCosts(task5));
        task5.addChild(task6, 1);
        assertEquals(2, task6.getNumDependency());
        assertEquals(1, task6.getCommunicationCosts(task5));
    }
}
