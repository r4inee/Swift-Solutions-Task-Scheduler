package swiftsolutions.taskscheduler.branchandbound;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Class that represents a schedule optimized for branch and bound.
 */
public class BNBSchedule {

    public static final int START_TIME = 0;
    public static final int END_TIME = 1;
    public static final int PROCCESSOR_INDEX = 2;
    public static final int EMPTY = -1;

    int _numTasks;
    int[][] _schedule;
    int[] _procEndTimes;
    int[] _firstTask;

    /**
     * Normal constructor
     * @param numTasks number of tasks to be scheduled
     * @param numProc number of processors to schedule on
     */
    public BNBSchedule(int numTasks, int numProc) {
        _numTasks = numTasks;
        _schedule = new int[_numTasks][3];
        _firstTask = new int[numTasks];

        // Initialize empty schedule
        for (int i = 0; i < _schedule.length; i++) {
            for (int j = 0; j < _schedule[0].length; j++) {
                _schedule[i][j] = _schedule[i][j] = EMPTY;
            }
            _firstTask[i] = EMPTY;
        }

        _procEndTimes = new int[numProc];
    }

    /**
     * Constructor used for cloning
     */
    public BNBSchedule(int numTasks, int[][] schedule, int[] procEndTimes, int[] firstTask) {
        _numTasks = numTasks;
        _schedule = schedule;
        _procEndTimes = procEndTimes;
        _firstTask = firstTask;
    }

    /**
     * Create a clone of the schedule
     * @return
     */
    public BNBSchedule copy() {
        // Make a deep copy of the schedule
        int[][] scheduleCopy = new int[_schedule.length][_schedule[0].length];
        for (int i = 0; i < _schedule.length; i++) {
            for (int j = 0; j < _schedule[0].length; j++) {
                scheduleCopy[i][j] = _schedule[i][j];
            }
        }

        return new BNBSchedule(_numTasks, scheduleCopy, Arrays.copyOf(_procEndTimes, _procEndTimes.length), _firstTask);
    }

    /**
     * Add a task to the schedule
     * @param task task to be added
     * @param proc processor for task to be scheduled on
     */
    public void addTask(BNBTask task, int proc) {
        // Initialize offset to 0
        int offset = 0;

        // Get earliest time that the task can be scheduled
        for (int parent : task._parents) {
            // Task must be scheduled after parent
            int tmpOffset = _schedule[parent][END_TIME];
            // If on a different processor, task must be ahead by
            // the communication cost
            if (proc != _schedule[parent][PROCCESSOR_INDEX]) {
                tmpOffset += task._commCost[parent];
            }
            if (tmpOffset > offset) {
                offset = tmpOffset;
            }
        }

        // If task is on a different processor, then we take offset
        // Else we can just use the processor end time.
        int taskStart;
        if (offset < _procEndTimes[proc]) {
            taskStart = _procEndTimes[proc];
        } else {
            taskStart = offset;
        }

        if (_firstTask[proc] == -1) {
            _firstTask[proc] = task._id;
        }

        // Update schedule and endProcTime to reflect the addition
        _procEndTimes[proc] = taskStart + task._procTime;
        _schedule[task._id][START_TIME] = taskStart;
        _schedule[task._id][END_TIME] = _procEndTimes[proc];
        _schedule[task._id][PROCCESSOR_INDEX] = proc;
    }


    /**
     * Get the time that the schedule takes
     * @return time that the schedule takes
     */
    public int getCost() {
        // Get the maximum time at which a processor ends
        int max = 0;
        for (int procCost : _procEndTimes) {
            if (procCost > max) {
                max = procCost;
            }
        }
        return max;
    }

    /**
     * Hashcode for pruning
     * @return hashcode that is equivalent to other "equivalent schedules"
     */
    @Override
    public int hashCode() {

        // Initialize some stacks representing a processor and a set representing the schedule
        Set<Stack<Integer>> schedule = new HashSet<>();
        Stack<Integer>[] stacks = new Stack[_procEndTimes.length];

        // Initialize the stacks
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = new Stack<>();
        }

        // Add each processors tasks and start times to a stack
        for (int i = 0; i < _schedule.length; i++) {
            if (_schedule[i][0] != -1) {
                stacks[_schedule[i][2]].add(i);
                stacks[_schedule[i][2]].add(_schedule[i][0]);
            }
        }

        // Add the stacks to a set.
        for(Stack<Integer> stack : stacks) {
            schedule.add(stack);
        }

        return schedule.hashCode();
    }

    /**
     * Used for pruning in hashset comparison, "equivalent schedules" should be equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        BNBSchedule other = (BNBSchedule)obj;

        return other.hashCode() == hashCode();
    }

    public int getFirstEmptyProc() {
        for (int i = 0; i < _firstTask.length; i++) {
            if (_firstTask[i] == EMPTY) {
                return i;
            }
        }
        return _firstTask.length;
    }
}
