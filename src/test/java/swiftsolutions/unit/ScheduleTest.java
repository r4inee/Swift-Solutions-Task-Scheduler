package swiftsolutions.unit;

import org.junit.Before;
import org.junit.Test;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Harith on 03/08/2018.
 */
public class ScheduleTest {

    // test schedules
    private Schedule _schedule1;
    private Schedule _schedule2;

    @Before
    public void testStateInit() {
        Map<Integer, Pair<Integer, Integer>> taskToProcAndStrtTime = new LinkedHashMap<>();
        taskToProcAndStrtTime.put(0, new Pair(0, 0)); // Pair(processor, startTime)
        taskToProcAndStrtTime.put(1, new Pair(0, 2));
        taskToProcAndStrtTime.put(2, new Pair(1, 0));
        taskToProcAndStrtTime.put(3, new Pair(1, 3));

        _schedule1 = new Schedule(taskToProcAndStrtTime, 2);

        Map<Integer, Pair<Integer, Integer>> taskToProcAndStrtTime2 = new LinkedHashMap<>();
        taskToProcAndStrtTime2.put(0, new Pair(0, 0)); // Pair(processor, startTime)
        taskToProcAndStrtTime2.put(1, new Pair(0, 2));
        taskToProcAndStrtTime2.put(2, new Pair(1, 0));
        taskToProcAndStrtTime2.put(3, new Pair(1, 3));
        taskToProcAndStrtTime2.put(4, new Pair(1, 6));
        taskToProcAndStrtTime2.put(5, new Pair(2, 4));
        taskToProcAndStrtTime2.put(6, new Pair(2, 5));

        _schedule2 = new Schedule(taskToProcAndStrtTime2, 2);

    }


    @Test
    public void testGetOutputString() {

        String expectedOutputString1 = "==========0==========\n"
                + "ID: 0 Start: 0 Processor: 0\n"
                + "ID: 1 Start: 2 Processor: 0\n"
                + "==========1==========\n"
                + "ID: 2 Start: 0 Processor: 1\n"
                + "ID: 3 Start: 3 Processor: 1\n";

        assertEquals(expectedOutputString1, _schedule1.getOutputString());



        String expectedOutputString2 = "==========0==========\n"
                + "ID: 0 Start: 0 Processor: 0\n"
                + "ID: 1 Start: 2 Processor: 0\n"
                + "==========1==========\n"
                + "ID: 2 Start: 0 Processor: 1\n"
                + "ID: 3 Start: 3 Processor: 1\n"
                + "ID: 4 Start: 6 Processor: 1\n"
                + "==========2==========\n"
                + "ID: 5 Start: 4 Processor: 2\n"
                + "ID: 6 Start: 5 Processor: 2\n";

        assertEquals(expectedOutputString2, _schedule2.getOutputString());

    }

    @Test
    public void testGetProcessor() {

        // notes:
        // -> proc, startTime order for Pair
        // -> -1 processTime means is arbitrary value (for when cannot infer from Schedule what it actually is)

        // schedule 1
        assertEquals(new Pair(0, 0), _schedule1.getProcessor(new Task(0, 2)));
        assertEquals(new Pair(0, 2), _schedule1.getProcessor(new Task(1, -1)));
        assertEquals(new Pair(1, 0), _schedule1.getProcessor(new Task(2, 3)));
        assertEquals(new Pair(1, 3), _schedule1.getProcessor(new Task(3, -1)));

        // schedule 2
        assertEquals(new Pair(0, 0), _schedule2.getProcessor(new Task(0, 2)));
        assertEquals(new Pair(0, 2), _schedule2.getProcessor(new Task(1, -1)));
        assertEquals(new Pair(1, 0), _schedule2.getProcessor(new Task(2, 3)));
        assertEquals(new Pair(1, 3), _schedule2.getProcessor(new Task(3, 3)));
        assertEquals(new Pair(1, 6), _schedule2.getProcessor(new Task(4, -1)));
        assertEquals(new Pair(2, 4), _schedule2.getProcessor(new Task(5, 1)));
        assertEquals(new Pair(2, 5), _schedule2.getProcessor(new Task(6, -1)));


    }

    @Test
    public void testGetNumProc() {

    }


}
