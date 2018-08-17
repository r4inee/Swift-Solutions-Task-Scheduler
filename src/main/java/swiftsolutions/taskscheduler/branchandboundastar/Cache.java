package swiftsolutions.taskscheduler.branchandboundastar;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Class for caching schedules using hashcodes.
 */
public class Cache extends HashSet {
    public static final int START_TIME = 0;
    public static final int END_TIME = 1;
    public static final int PROCESSOR_INDEX = 2;
    public static final int EMPTY = -1;

    public int _numProc;
    public int[][] _schedule;

    /**
     * Constructor for the cache schedule.
     * @param numProc Total number of processors
     * @param schedule The current partial schedule
     */
    public Cache (int numProc, int[][] schedule) {
        _schedule = schedule;
        _numProc = numProc;
    }

    @Override
    public int hashCode() {
        // Initialize some stacks representing a processor and a set representing the schedule
        Set<Stack<Integer>> schedule = new HashSet<>();
        Stack<Integer>[] stacks = new Stack[_numProc];

        // Initialize the stacks
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = new Stack<>();
        }

        // Add each processors tasks and start times to a stack
        for (int i = 0; i < _schedule.length; i++) {
            if (_schedule[i][START_TIME] != EMPTY) {
                stacks[_schedule[i][PROCESSOR_INDEX]].add(i);
                stacks[_schedule[i][PROCESSOR_INDEX]].add(_schedule[i][START_TIME]);
            }
        }

        // Add the stacks to a set.
        for(Stack<Integer> stack : stacks) {
            schedule.add(stack);
        }

        return schedule.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        Cache other = (Cache) obj;
        return other.hashCode() == hashCode();
    }
}
