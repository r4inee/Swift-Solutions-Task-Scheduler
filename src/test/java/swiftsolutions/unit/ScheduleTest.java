package swiftsolutions.unit;

import org.junit.Before;
import org.junit.Test;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Harith on 03/08/2018.
 */
public class ScheduleTest {

    //

    @Before
    public void testStateInit() {

    }


    @Test
    public void testGetOutputString() {

        Map<Integer, Pair<Integer, Integer>> taskToProcAndStrtTime = new LinkedHashMap<>();
        taskToProcAndStrtTime.put(0, new Pair(0, 0)); // Pair(processor, startTime)
        taskToProcAndStrtTime.put(1, new Pair(0, 2));
        taskToProcAndStrtTime.put(2, new Pair(1, 0));
        taskToProcAndStrtTime.put(3, new Pair(1, 3));



        Schedule schedule = new Schedule(taskToProcAndStrtTime, 2);

/*        Map<Integer, ArrayList<Integer>> expectedProcMap = new LinkedHashMap<>();

        expectedProcMap.put(0, new ArrayList<Integer>() {
            {
                add(0); add(1);
            }
        });
        expectedProcMap.put(1, new ArrayList<Integer>() {
            {
                add(2); add(3);
            }
        });

        assertEquals(expectedProcMap, schedule.)*/
        String expectedOutputString = "==========0==========\n"
                + "ID: 0 Start: 0 Processor: 0\n"
                + "ID: 1 Start: 2 Processor: 0\n"
                + "==========1==========\n"
                + "ID: 2 Start: 0 Processor: 1\n"
                + "ID: 3 Start: 3 Processor: 1\n";

        assertEquals(expectedOutputString, schedule.getOutputString());


    }

    @Test
    public void testGetProcessor() {

    }

    @Test
    public void testGetNumProc() {

    }


}
