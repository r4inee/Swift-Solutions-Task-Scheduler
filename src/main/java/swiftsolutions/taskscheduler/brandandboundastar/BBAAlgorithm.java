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
    private int[][] _parentMap; // row = task, col = parents, value = 1 or 0
    private int[][] _comCostMap; // row = task, col = parent, value = communication cost
    private int[][] _bestFState; // output
    private List<Integer> _scheduledTasks; // scheduled tasks

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
    private int BBA(int currentTask, int currentProcessor, int previousTask,
                     int previousProcessor, int numFreeTasks, int depth, int[][] s, int B){
        int done = 0; //exit flag
        boolean backtrack = false; //backtrack flag
        Queue<Integer> freeTasks = free(); //priority queue for tasks based on cost function
        int[][] processorIndex = new int[_numProcessors][]; // index for each processor
        if (freeTasks.size() != 0) {
            for (int i = 0; i < numFreeTasks; i++) {
                int poppedTask = freeTasks.remove(); //take the first task of the queue
                for (int j = 0; j < _numProcessors; j++) { //add the task to all processors
                    depth++;
                    if (backtrack){
                        sanitizeSchedule(s);
                    }
                    numFreeTasks = freeTasks.size();
                    addTask(s,poppedTask,j,processorIndex[j][0]);
                    processorIndex[j][0]++;
                    previousTask = currentTask;
                    previousProcessor = currentProcessor;
                    currentTask = poppedTask;
                    currentProcessor = j;
                    if (cost(s) <= B && depth == _taskMap.length){
                        _bestFState = s;
                        B = cost(s);
                        return 1;
                    }
                    if (cost(s) <= B && depth <= _taskMap.length){
                        done = BBA(currentTask,currentProcessor,previousTask,previousProcessor,numFreeTasks,depth,s,B);
                    }
                    if (done == 0){
                        backtrack=true;
                        depth--;
                    }

                }
            }
        }
        return 1;
    }

    private int cost(int[][] s) {
        return 1;
    }

    private void sanitizeSchedule(int[][] s) {

    }

    private void addTask(int[][] s, int task, int processor, int processorIndex){
        s[processor][processorIndex] = task;
        _scheduledTasks.add(task);
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

    private void listHeuristic(){

    }
}
