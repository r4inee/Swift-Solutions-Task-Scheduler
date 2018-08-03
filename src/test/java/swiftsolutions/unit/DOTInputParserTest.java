//package swiftsolutions.unit;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//import swiftsolutions.exceptions.InputException;
//import swiftsolutions.input.DOTInputParser;
//import swiftsolutions.taskscheduler.Task;
//
//import java.util.Set;
//
//import static junit.framework.Assert.assertEquals;
//import static junit.framework.Assert.assertNotNull;
//import static junit.framework.Assert.fail;
//
//
///**
// * This contains basic unit tests for testing parsing of graph data from different input formats.
// */
//
//public class DOTInputParserTest {
//
//    @Test
//    public void testParseNoHeaders() {
//        DOTInputParser parser = new DOTInputParser();
//        Set<Task> allTasks = null;
//        try {
//            allTasks = parser.parse("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
//        } catch (InputException e) {
//            e.printStackTrace();
//            fail();
//        }
//
//        assertNotNull(allTasks);
//        assertEquals(7, allTasks.size());
//
//        for (Task task : allTasks) {
//            switch (task.getTaskID()) {
//                case 0:
//                    assertEquals(0, task.getNumDependency());
//                    break;
//                case 1:
////                    assertEquals(1, task.getNumDependency());
//                    break;
//                case 2:
//                    assertEquals(1, task.getNumDependency());
//                    break;
//                case 3:
//                    assertEquals(1, task.getNumDependency());
//                    break;
//                case 4:
//                    assertEquals(1, task.getNumDependency());
//                    break;
//                case 5:
//                    assertEquals(1, task.getNumDependency());
//                    break;
//                case 6:
//                    assertEquals(1, task.getNumDependency());
//                    break;
//                default:
//                    fail("Incorrectly parsed edges of the graph");
//            }
//        }
//    }
//}
//
