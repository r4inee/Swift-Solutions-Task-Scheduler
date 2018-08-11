package swiftsolutions.taskscheduler.brandandboundastar;

import org.omg.PortableInterceptor.INACTIVE;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.util.*;

public class BBAAlgorithm implements Algorithm{

    private int _numProcessors;
    private int[][] _taskMap; // rows = task, col = {id, process time, number of dependencies, bottom-level}
    private int[][] _processMap; // rows = processor, col = end time
    private int[][] _scheduleMap; // rows = processor, col = end time
    private List<Integer[][]> _schedules;
    private Map<Integer, Pair<Integer, Integer>> _scheduleInheritanceMap; // maps a schedule to parent and children
    private Map<Integer, Integer[][]> _scheduleIDMap; // maps an ID for a schedule
    private int[][] _parentMap; // row = task, col = parents, value = 1 or 0
    private int[][] _comCostMap; // row = task, col = parent, value = communication cost
    private int[][] _bestFState; // output
    private int[] _scheduledTasks; // scheduled tasks

    // used for schedules in general (including _bestFState)
    public static final int POS_PROC = 0;
    // used for _taskMap
    public static final int TASK_ID = 0;
    public static final int TASK_COST = 1;
    public static final int NUM_DEP = 2;
    public static final int BOTT_LVL = 3;
    // used for processMap
    public static final int END_TIME = 0;



    /**
     * Overrides Algorithm setProcessors
     * See Algorithm#setProcessors()
     * @param processors
     */
    @Override
    public void setProcessors(int processors) {
        _numProcessors = processors;
    }

    /**
     * Overrides Algorithm execute
     * See Algorithm#execute()
     * @param tasks tasks that will be scheduled
     * @return
     */
    @Override
    public Schedule execute(Map<Integer, Task> tasks) {





        return null;
    }

    /**
     * REDO THIS
     * BBA algorithm
     * B is the overestimate of the schedule length
     * bestFState is lower f-value of the best schedule
     * @param currentTask
     * @param currentProcessor
     * @param previousTask
     * @param previousProcessor
     * @param numFreeTasks
     * @param depth
     * @param B
     */
    private void BBA(int currentTask, int currentProcessor, int previousTask,
                     int previousProcessor, int numFreeTasks, int depth, int[][] s, int B){
        int done = 0; //exit flag
        Queue<Integer> freeTasks = free();
        if (freeTasks.size() != 0) {
            for (int i = 0; i < numFreeTasks; i++) {
                for (int j = 0; j < _numProcessors; j++) {
                    depth++;
                    sanitizeSchedule();
                    numFreeTasks = freeTasks.size();

                }
            }
        }
    }

    private void addTask(int[][] s, int task, int processor){

    }

    private Queue<Integer> free() {
        Set<Integer> freeTasksSet = new HashSet<>();
        for (int[] a_taskMap : _taskMap) {
            // check if task has all its parents scheduled
            if (a_taskMap[NUM_DEP] == 0) {
                freeTasksSet.add(a_taskMap[TASK_ID]);
            }
        }
        // remove any task from the set that themselves have already been scheduled
        for (int _scheduledTask : _scheduledTasks) {
            if (freeTasksSet.contains(_scheduledTask)) {
                freeTasksSet.remove(_scheduledTask);
            }
        }
        // convert to int[]
        int[] freeTasks = freeTasksSet.stream().mapToInt(Integer::intValue).toArray();
        Queue<Integer> freeTaskQueue = orderFreeTasks(freeTasks);
        return freeTaskQueue;
    }

    private Queue<Integer> orderFreeTasks(int[] freeTasks){
        return null;
    }

    private void sanitizeSchedule(){

    }
    private void listHeuristic(){

    }
}
