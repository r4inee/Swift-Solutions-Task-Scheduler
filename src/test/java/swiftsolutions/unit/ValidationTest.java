package swiftsolutions.unit;

import org.junit.Test;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.taskscheduler.branchandbound.BNBAlgorithm;
import swiftsolutions.util.Pair;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class ValidationTest {

    @Test
    public void testNode7OutTree() {
        DOTInputParser parser = new DOTInputParser();
        Map<Integer, Task> taskMap = new HashMap<>();
        try {
            taskMap =  parser.parse("src/test/resources/test_graphs/Nodes_7_OutTree.dot");
        } catch (InputException e) {
            e.printStackTrace();
        }

        Algorithm algorithm = new BNBAlgorithm();
        algorithm.setProcessors(2);
        Schedule outputSchedule = algorithm.execute(taskMap);

        Map<Integer, Pair<Integer, Integer>> schedule = outputSchedule.getTaskToProcessorMap();
        int maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 28);

        algorithm = new BNBAlgorithm();
        algorithm.setProcessors(4);
        outputSchedule = algorithm.execute(taskMap);

        schedule = outputSchedule.getTaskToProcessorMap();
        maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 22);
    }

    @Test
    public void testNode8Random() {
        DOTInputParser parser = new DOTInputParser();
        Map<Integer, Task> taskMap = new HashMap<>();
        try {
            taskMap =  parser.parse("src/test/resources/test_graphs/Nodes_8_Random.dot");
        } catch (InputException e) {
            e.printStackTrace();
        }

        Algorithm algorithm = new BNBAlgorithm();
        algorithm.setProcessors(2);
        Schedule outputSchedule = algorithm.execute(taskMap);

        Map<Integer, Pair<Integer, Integer>> schedule = outputSchedule.getTaskToProcessorMap();
        int maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 581);

        algorithm = new BNBAlgorithm();
        algorithm.setProcessors(4);
        outputSchedule = algorithm.execute(taskMap);

        schedule = outputSchedule.getTaskToProcessorMap();
        maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 581);
    }

    @Test
    public void testNode9SeriesParallel() {
        DOTInputParser parser = new DOTInputParser();
        Map<Integer, Task> taskMap = new HashMap<>();
        try {
            taskMap =  parser.parse("src/test/resources/test_graphs/Nodes_9_SeriesParallel.dot");
        } catch (InputException e) {
            e.printStackTrace();
        }

        Algorithm algorithm = new BNBAlgorithm();
        algorithm.setProcessors(2);
        Schedule outputSchedule = algorithm.execute(taskMap);

        Map<Integer, Pair<Integer, Integer>> schedule = outputSchedule.getTaskToProcessorMap();
        int maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 55);

        algorithm = new BNBAlgorithm();
        algorithm.setProcessors(4);
        outputSchedule = algorithm.execute(taskMap);

        schedule = outputSchedule.getTaskToProcessorMap();
        maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 55);
    }

    @Test
    public void testNode10Random() {
        DOTInputParser parser = new DOTInputParser();
        Map<Integer, Task> taskMap = new HashMap<>();
        try {
            taskMap =  parser.parse("src/test/resources/test_graphs/Nodes_10_Random.dot");
        } catch (InputException e) {
            e.printStackTrace();
        }

        Algorithm algorithm = new BNBAlgorithm();
        algorithm.setProcessors(2);
        Schedule outputSchedule = algorithm.execute(taskMap);

        Map<Integer, Pair<Integer, Integer>> schedule = outputSchedule.getTaskToProcessorMap();
        int maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 50);

        algorithm = new BNBAlgorithm();
        algorithm.setProcessors(4);
        outputSchedule = algorithm.execute(taskMap);

        schedule = outputSchedule.getTaskToProcessorMap();
        maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 50);
    }

    @Test
    public void testNode11OutTree() {
        DOTInputParser parser = new DOTInputParser();
        Map<Integer, Task> taskMap = new HashMap<>();
        try {
            taskMap =  parser.parse("src/test/resources/test_graphs/Nodes_11_OutTree.dot");
        } catch (InputException e) {
            e.printStackTrace();
        }

        Algorithm algorithm = new BNBAlgorithm();
        algorithm.setProcessors(2);
        Schedule outputSchedule = algorithm.execute(taskMap);

        Map<Integer, Pair<Integer, Integer>> schedule = outputSchedule.getTaskToProcessorMap();
        int maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 350);

        algorithm = new BNBAlgorithm();
        algorithm.setProcessors(4);
        outputSchedule = algorithm.execute(taskMap);

        schedule = outputSchedule.getTaskToProcessorMap();
        maxEndTime = 0;
        for (Integer task : schedule.keySet()) {
            int endTime = schedule.get(task).getB() + taskMap.get(task).getProcessTime();
            if (endTime > maxEndTime) {
                maxEndTime = endTime;
            }
        }
        assertEquals(maxEndTime, 227);
    }

}
