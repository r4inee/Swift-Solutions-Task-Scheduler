package swiftsolutions.taskscheduler.brandandboundastar;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class BBAAlgorithm implements Algorithm {
    private int _numProcessors;

    private int[][] _tasks; // row represents the task, cols represent { proc time, number of dependencies, bottom level}
    private int[][] _dependencies; // row represents child, col represents parent, value 1 represents is parent 0 if not
    private int[][] _bestFState; // output schedule
    private int[][] _communicationCosts; // row represents the parent, col represents the child, value is the cost
    private int[][] _nodeEquivalence;
    private Map<Integer, Task> _taskMap;
    private Set<Cache> _seenSchedules;
    private int _B;
    private int _fSInit;

    public static final int EMPTY = -1;
    public static final int SCHEDULE_COL_SIZE = 3;
    // used for schedules in general (including _bestFState)
    public static final int START_TIME = 0;
    public static final int END_TIME = 1;
    public static final int PROCESSOR_INDEX = 2;
    // used for _tasks
    public static final int PROC_TIME = 0;
    public static final int NUM_DEP = 1;
    public static final int BOTTOM_LVL = 2;

    public BBAAlgorithm() {
        _seenSchedules = new HashSet<>();
    }

    /**
     * Overrides Algorithm setProcessors
     * See Algorithm#setProcessors()
     *
     * @param processors
     */
    @Override
    public void setProcessors(int processors) {
        _numProcessors = processors;
    }

    /**
     * Overrides Algorithm execute
     * See Algorithm#execute()
     *
     * @param tasks tasks that will be scheduled
     * @return
     */
    @Override
    public Schedule execute(Map<Integer, Task> tasks) {
        _taskMap = tasks;
        Set<Task> leafs = tasks.values() //find all the leaf nodes
                .stream()
                .filter((Task task) -> task.getChildTasks().size() == 0)
                .collect(Collectors.toSet());
        for (Task leaf : leafs) { //Compute the bottom levels for the nodes
            leaf.updateBottomLevel(leaf.getProcessTime());
            getBottomLevels(leaf.getParentTasks(), leaf.getProcessTime());
        }
        convertTasks(); //converts the tasks into the 2D array format
        int[] procEndTimes = new int[_numProcessors]; // create a 2D array with row size number of processors, 1 col
        int[][] initialSchedule = new int[_tasks.length][SCHEDULE_COL_SIZE];
        // need to make the processor value on initial schedule -1;
        for (int i = 0; i < initialSchedule.length; i++) {
            initialSchedule[i][PROCESSOR_INDEX] = EMPTY;
            initialSchedule[i][START_TIME] = EMPTY;
            initialSchedule[i][END_TIME] = EMPTY;
        }
        _B = 0; // Max int
        int maxBotLevel = 0;
        for (int task : _taskMap.keySet()) {
            _B += _taskMap.get(task).getProcessTime();
            if (_taskMap.get(task).getBottomLevel() > maxBotLevel) {
                maxBotLevel = _taskMap.get(task).getBottomLevel();
            }
        }
        _nodeEquivalence = new int[tasks.size()][tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = 0; j < tasks.size(); j++) {
                if (nodeEquivalence(i, j)) {
                    _nodeEquivalence[i][j] = 1;
                }
            }
        }
        int idleTime = 0;

        _fSInit = Math.max(_B / _numProcessors, maxBotLevel);
        Queue<Integer> initialB = new PriorityQueue<>((o1, o2) -> {
            Integer blA = _tasks[o1][BOTTOM_LVL];
            Integer blB = _tasks[o2][BOTTOM_LVL];
            return blB.compareTo(blA);
        });
        initialB.addAll(_taskMap.keySet());
        int[] freeTasks = new int[tasks.size()];
        for (int i = 0; i < freeTasks.length; i++) {
            freeTasks[i] = initialB.poll();
        }

        int[] fTask = free(initialSchedule, _tasks);
        if ((isAllIndependent(freeTasks)) || ((isFTO(freeTasks, initialSchedule)) && (fTask.length == freeTasks.length))) {
            BBA(EMPTY, EMPTY, EMPTY, EMPTY,
                    fTask.length, 0, procEndTimes, _tasks, initialSchedule, idleTime);
        } else {
            FTO(freeTasks, procEndTimes, _tasks, initialSchedule, idleTime);
            BBA(EMPTY, EMPTY, EMPTY, EMPTY,
                    fTask.length, 0, procEndTimes, _tasks, initialSchedule, idleTime); //Call the recursion algorithm
        }
        return convertSchedule(_bestFState);
    }

    /**
     * This is the main method that creates the schedules implemented using the pseudo code. Hybrid Branch and Bound
     * with A* algorithm
     *
     * @param currentTask
     * @param currentProcessor
     * @param previousTask
     * @param previousProcessor
     * @param numFreeTasks
     * @param depth
     */
    private void BBA(int currentTask, int currentProcessor, int previousTask,
                     int previousProcessor, int numFreeTasks, int depth, int[] procEndTimes, int[][] tasks, int[][] s, int idleTime) {
        //priority queue for tasks based on cost function
        int[] freeTasks = free(s, tasks);
        if (freeTasks.length != 0) {
            if (isAllIndependent(freeTasks)) {
                Comparator<Integer> c = (Integer o1, Integer o2) -> {
                    Integer o1Process = _tasks[o1][PROC_TIME];
                    Integer o2Process = _tasks[o2][PROC_TIME];
                    return o2Process.compareTo(o1Process);
                };
                Queue<Integer> queue = new PriorityQueue<>(freeTasks.length, c);
                for (int i = 0; i < freeTasks.length; i++) {
                    queue.add(freeTasks[i]);
                }
                int[] orderedTasks = new int[freeTasks.length];
                for (int i = 0; i < freeTasks.length; i++) {
                    orderedTasks[i] = queue.poll();
                }
                FTO(orderedTasks, procEndTimes, tasks, s, idleTime);
                return;
            } else if (isFTO(freeTasks, s) && (freeTasks.length == numFreeTasks)) {
                Comparator<Integer> c = (o1, o2) -> {
                    Integer parentO1DRT = 0;
                    int o1Parents = _taskMap.get(o1).getParentTasks().size();
                    int o1Children = _taskMap.get(o1).getChildTasks().size();
                    Integer parentO2DRT = 0;
                    int o2Parents = _taskMap.get(o2).getParentTasks().size();
                    int o2Children = _taskMap.get(o2).getChildTasks().size();

                    if (o1Parents == 1) {
                        int parent = _taskMap.get(o1).getParentTasks().iterator().next();
                        parentO1DRT = s[parent][END_TIME] + _communicationCosts[parent][o1];
                    }
                    if (o2Parents == 1) {
                        int parent = _taskMap.get(o2).getParentTasks().iterator().next();
                        parentO2DRT = s[parent][END_TIME] + _communicationCosts[parent][o2];
                    }

                    if (parentO1DRT.intValue() == parentO2DRT.intValue()) {
                        Integer outO1 = 0;
                        Integer outO2 = 0;
                        if (o1Children == 1) {
                            int child = _taskMap.get(o1).getChildTasks().iterator().next();
                            outO1 = _communicationCosts[o1][child];
                        }
                        if (o2Children == 1) {
                            int child = _taskMap.get(o2).getChildTasks().iterator().next();
                            outO2 = _communicationCosts[o2][child];
                        }
                        return outO2.compareTo(outO1);
                    }

                    return parentO1DRT.compareTo(parentO2DRT);
                };
                Queue<Integer> queue = new PriorityQueue<>(freeTasks.length, c);
                for (int j = 0; j < freeTasks.length; j++) {
                    queue.add(freeTasks[j]);
                }

                if (queue.size() > 1) {
                    int[] orderedTasks = new int[queue.size()];
                    boolean order = true;
                    for (int j = 0; j < orderedTasks.length; j++) {
                        orderedTasks[j] = queue.poll();
                        if (j > 1) {
                            int o1 = orderedTasks[j - 1];
                            int o2 = orderedTasks[j];

                            int outO1 = 0;
                            int outO2 = 0;
                            if (_taskMap.get(o1).getChildTasks().size() == 1) {
                                outO1 = _communicationCosts[o1][_taskMap.get(o1).getChildTasks().iterator().next()];
                            }
                            if (_taskMap.get(o2).getChildTasks().size() == 1) {
                                outO2 = _communicationCosts[o2][_taskMap.get(o2).getChildTasks().iterator().next()];
                            }

                            if (outO2 < outO1) {
                                order = false;
                                break;
                            }
                        }
                    }
                    if (order) {
                        FTO(orderedTasks, procEndTimes, tasks, s, idleTime);
                        return;
                    }
                }
            }

            Cache cache = new Cache(_numProcessors, s);
            if (_seenSchedules.contains(cache)) {
                return;
            } else {
                _seenSchedules.add(cache);
            }

            for (int i = 0; i < freeTasks.length; i++) {
                for (int j = 0; j < _numProcessors; j++) { //add the task to all processors
                    if (j > getFirstEmptyProc(procEndTimes)) {
                        break;
                    }

                    // Partial Duplicate Detection
                    if (freeTasks.length == numFreeTasks) {
                        if (j < previousProcessor) {
                            continue;
                        }
                    }
                    int taskID = freeTasks[i];
                    currentProcessor = j;
                    currentTask = taskID;
                    // Node Equivalence
                    if (previousTask != EMPTY) {
                        if (_nodeEquivalence[previousTask][currentTask] == 1) {
                            break;
                        }
                    }
                    int[][] clonedS = copySchedule(s);
                    int[] clonedProcEndTimes = Arrays.copyOf(procEndTimes, procEndTimes.length); //copy Processor end times
                    int[][] clonedTasks = copyTasks(tasks);
                    //select task to add
                    //calculate parent offset
                    int offset = 0;
                    for (int di = 0; di < _dependencies[taskID].length; di++) {
                        int tempOffset = clonedS[di][END_TIME];
                        //look at all parents of current task (parent task id is DJ)
                        if (_dependencies[taskID][di] == 1) {
                            //check if that parent is on the same proc
                            if (clonedS[di][PROCESSOR_INDEX] != j) {
                                //if the processor is not on the same
                                tempOffset += _communicationCosts[di][taskID];
                            }
                            if (tempOffset > offset) {
                                offset = tempOffset;
                            }
                        }
                    }
                    int taskStart;
                    if (offset < clonedProcEndTimes[j]) {
                        taskStart = clonedProcEndTimes[j];
                    } else if (clonedProcEndTimes[j] == 0) {
                        taskStart = offset;
                        idleTime = offset;
                    } else {
                        taskStart = offset;
                        idleTime += offset - clonedProcEndTimes[j];
                    }
                    clonedS[taskID][PROCESSOR_INDEX] = j;
                    clonedS[taskID][START_TIME] = taskStart;
                    clonedS[taskID][END_TIME] = taskStart + clonedTasks[taskID][PROC_TIME];
                    clonedProcEndTimes[j] = clonedS[taskID][END_TIME];
                    for (int dj = 0; dj < _dependencies.length; dj++) {
                        if (_dependencies[dj][taskID] == 1) {
                            clonedTasks[dj][NUM_DEP]--;
                        }
                    }
                    previousTask = previousTask; //reset method values
                    previousProcessor = previousProcessor;
                    numFreeTasks = free(clonedS, clonedTasks).length;
                    depth++;
                    //if cost is lower than B(est) and depth is max, set current best, go back up tree
                    if (cost(clonedS, clonedProcEndTimes, currentTask, offset, idleTime, freeTasks) <= _B && depth == _tasks.length) {
                        _bestFState = clonedS; // clonedS
                        _B = cost(clonedS, clonedProcEndTimes, currentTask, offset, idleTime, freeTasks);
                    }
                    //if cost is lower than B(est) and depth is max, recursive call
                    if (cost(clonedS, clonedProcEndTimes, currentTask, offset, idleTime, freeTasks) <= _B && depth < _tasks.length) {
                        // Scheduling Caching
                        BBA(currentTask, currentProcessor, currentTask, currentProcessor, numFreeTasks, depth, clonedProcEndTimes, clonedTasks, clonedS, idleTime);
                    }
                    numFreeTasks = freeTasks.length;
                    depth--;
                    if (offset > clonedProcEndTimes[j]) {
                        idleTime -= offset - clonedProcEndTimes[j];
                    }
                }
            }
        }
    }

    private void FTO(int[] orderedTasks, int[] procEndTimes, int[][] tasks, int[][] s, int idleTime) {
        int task = orderedTasks[0];
        int[] newOrder = new int[orderedTasks.length - 1];
        for (int i = 1; i < orderedTasks.length; i++) {
            newOrder[i - 1] = orderedTasks[i];
        }

        for (int j = 0; j < _numProcessors; j++) { //add the task to all processors
            if (j > getFirstEmptyProc(procEndTimes)) {
                break;
            }
            int[][] clonedS = copySchedule(s);
            int[] clonedProcEndTimes = Arrays.copyOf(procEndTimes, procEndTimes.length); //copy Processor end times
            int[][] clonedTasks = copyTasks(tasks);
            //calculate parent offset
            int offset = 0;
            for (int di = 0; di < _dependencies[task].length; di++) {
                int tempOffset = clonedS[di][END_TIME];
                //look at all parents of current task (parent task id is DJ)
                if (_dependencies[task][di] == 1) {
                    //check if that parent is on the same proc
                    if ((clonedS[di][PROCESSOR_INDEX] != j) && (clonedS[di][PROCESSOR_INDEX] != EMPTY)) {
                        //if the processor is not on the same
                        tempOffset += _communicationCosts[di][task];
                    }
                    if (tempOffset > offset) {
                        offset = tempOffset;
                    }
                }
            }
            int taskStart;
            if (offset < clonedProcEndTimes[j]) {
                taskStart = clonedProcEndTimes[j];
            } else if (clonedProcEndTimes[j] == 0) {
                taskStart = offset;
                idleTime = offset;
            } else {
                taskStart = offset;
                idleTime += offset - clonedProcEndTimes[j];
            }
            clonedS[task][PROCESSOR_INDEX] = j;
            clonedS[task][START_TIME] = taskStart;
            clonedS[task][END_TIME] = taskStart + clonedTasks[task][PROC_TIME];
            clonedProcEndTimes[j] = clonedS[task][END_TIME];
            for (int dj = 0; dj < _dependencies.length; dj++) {
                if (_dependencies[dj][task] == 1) {
                    clonedTasks[dj][NUM_DEP]--;
                }
            }
            if (newOrder.length == 0) {
                if (cost(clonedS, clonedProcEndTimes, task, offset, idleTime, newOrder) <= _B) {
                    _bestFState = clonedS; // clonedS
                    _B = cost(clonedS, clonedProcEndTimes, task, offset, idleTime, newOrder);
                }
            } else {
                FTO(newOrder, clonedProcEndTimes, clonedTasks, clonedS, idleTime);
            }

            if (offset > clonedProcEndTimes[j]) {
                idleTime -= offset - clonedProcEndTimes[j];
            }
        }
    }

    /**
     * Method to make a copy of a schedule
     *
     * @param s
     * @return
     */
    private int[][] copySchedule(int[][] s) {
        int[][] clonedS = new int[s.length][s[START_TIME].length]; //START_TIME used to just to find length
        for (int si = 0; si < s.length; si++) { //copies the schedule instead of re-reference, tasks are removed if branch moves up
            for (int sj = 0; sj < s[si].length; sj++) {
                clonedS[si][sj] = s[si][sj];
            }
        }
        return clonedS;
    }

    /**
     * Method to make a copy of tasks
     *
     * @param tasks
     * @return
     */
    private int[][] copyTasks(int[][] tasks) {
        int[][] clonedTasks = new int[tasks.length][tasks[PROC_TIME].length]; //copies the tasks
        for (int ti = 0; ti < tasks.length; ti++) {
            for (int tj = 0; tj < tasks[ti].length; tj++) {
                clonedTasks[ti][tj] = tasks[ti][tj];
            }
        }
        return clonedTasks;
    }

    /**
     * Recursive function that updates the bottom levels
     * of the nodes in the input set and all
     * the nodes of that parent
     *
     * @param nodes
     * @param currentBottomLevel
     */
    private void getBottomLevels(Set<Integer> nodes, int currentBottomLevel) {
        for (Integer node : nodes) {
            if (_taskMap.get(node).getBottomLevel() < currentBottomLevel + _taskMap.get(node).getProcessTime()) {
                _taskMap.get(node).updateBottomLevel(currentBottomLevel +
                        _taskMap.get(node).getProcessTime());
            }
            if (!_taskMap.get(node).getParentTasks().isEmpty()) {
                getBottomLevels(_taskMap.get(node).getParentTasks(),
                        _taskMap.get(node).getBottomLevel());
            }
        }
    }

    /**
     * Based off the cost function f described where
     * f = max{Initial(s), idle-time(s), bottom-level(s), DRT(s)}
     *
     * @param s
     * @return
     */
    private int cost(int[][] s, int[] procEndTimes, int taskID, int dataReadyTime, int idleTime, int[] freeTasks) {
        //cost of the initial state
        int cost = _fSInit;
        for (int i = 0; i < procEndTimes.length; i++) {
            if (procEndTimes[i] > cost) {
                cost = procEndTimes[i];
            }
        }
        // get the bottom level of the schedule
        int scheduleBottomLevel = s[taskID][START_TIME] + _tasks[taskID][BOTTOM_LVL];
        if (scheduleBottomLevel > cost) {
            cost = scheduleBottomLevel;
        }
        if (idleTime > cost) {
            cost = idleTime + _tasks[taskID][PROC_TIME];
        }
        if (dataReadyTime > cost) {
            cost = dataReadyTime + _tasks[taskID][PROC_TIME];
        }
        return cost;
    }

    /**
     * This method takes in a schedule and returns an array of free tasks
     * which are tasks that are not already scheduled and there parents
     * have all been scheduled
     *
     * @param s
     * @return
     */
    private int[] free(int[][] s, int[][] tasks) {
        Set<Integer> taskSet = new HashSet<>();
        for (int i = 0; i < tasks.length; i++) {
            taskSet.add(i);
        }
        //loop through all tasks on all processors
        for (int i = 0; i < tasks.length; i++) {
            // if it is scheduled on a processor remove it from the set
            if (s[i][PROCESSOR_INDEX] != EMPTY) {
                taskSet.remove(i);
            }
            // if not all parents have been scheduled then remove it form the set
            if (tasks[i][NUM_DEP] != 0) {
                taskSet.remove(i);
            }
        }

        int[] freeTasks = taskSet.stream().mapToInt(Number::intValue).toArray();
        return freeTasks;
    }

    /**
     * This method is used to convertTasks the task map passed into
     * execute() into the arrays used to map the data in this
     * class
     */
    private void convertTasks() {
        //initialize _tasks array
        _tasks = new int[_taskMap.size()][3];
        //Parse the Task into the 2D array tasks
        for (int taskID : _taskMap.keySet()) {
            _tasks[taskID][PROC_TIME] = _taskMap.get(taskID).getProcessTime();
            _tasks[taskID][NUM_DEP] = _taskMap.get(taskID).getNumDependency();
            _tasks[taskID][BOTTOM_LVL] = _taskMap.get(taskID).getBottomLevel();
        }
        //initialise _dependencies + _commcosts
        _dependencies = new int[_taskMap.size()][_taskMap.size()];
        _communicationCosts = new int[_taskMap.size()][_taskMap.size()];
        //Parse the parents into the 2D array dependencies
        for (int taskID : _taskMap.keySet()) {
            for (int parentID : _taskMap.get(taskID).getParentTasks()) {
                _dependencies[taskID][parentID] = 1;
                _communicationCosts[parentID][taskID] = _taskMap.get(taskID).getCommunicationCosts(parentID);
            }
        }
    }

    /**
     * Convert the final 2D array schedule back into a schedule that is to be
     * returned be the execute method
     *
     * @param schedule
     * @return
     */
    private Schedule convertSchedule(int[][] schedule) {
        Map<Integer, Pair<Integer, Integer>> scheduleMap = new LinkedHashMap<>();
        for (int i = 0; i < schedule.length; i++) {
            int startTime = schedule[i][START_TIME];
            int processor = schedule[i][PROCESSOR_INDEX];
            scheduleMap.put(i, new Pair<>(processor, startTime));
        }
        return new Schedule(scheduleMap, _numProcessors);
    }

    private int getFirstEmptyProc(int[] procEndTimes) {
        for (int i = 0; i < _numProcessors; i++) {
            if (procEndTimes[i] == 0) {
                return i;
            }
        }
        return _numProcessors;
    }

    private boolean isAllIndependent(int[] freeTask) {
        for (int i = 0; i < freeTask.length; i++) {
            if (_taskMap.get(freeTask[i]).getParentTasks().size() != 0) {
                return false;
            }
            if (_taskMap.get(freeTask[i]).getChildTasks().size() != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isFTO(int[] freeTasks, int[][] schedule) {
        for (int i = 0; i < freeTasks.length; i++) {
            int parents = _taskMap.get(freeTasks[i]).getParentTasks().size();
            int children = _taskMap.get(freeTasks[i]).getChildTasks().size();

            if ((parents <= 1) && (children <= 1)) {
                if ((parents == 0) && (children == 0)) {
                    continue;
                }

                if (children == 1) {
                    int tChild = _taskMap.get(freeTasks[i]).getChildTasks().iterator().next();
                    for (int j = 0; j < freeTasks.length; j++) {
                        int numChild = _taskMap.get(freeTasks[j]).getChildTasks().size();
                        if (numChild == 1) {
                            int child = _taskMap.get(freeTasks[j]).getChildTasks().iterator().next();
                            if (child != tChild) {
                                return false;
                            }
                        } else if (numChild > 1) {
                            return false;
                        }
                    }
                }

                if (parents == 1) {
                    int proc = EMPTY;
                    for (int j = 0; j < freeTasks.length; j++) {
                        int numParent = _taskMap.get(freeTasks[j]).getParentTasks().size();
                        if (numParent == 1) {
                            int parent = _taskMap.get(freeTasks[j]).getParentTasks().iterator().next();
                            if (proc == EMPTY) {
                                proc = schedule[parent][PROCESSOR_INDEX];
                            } else {
                                if (proc != schedule[parent][PROCESSOR_INDEX]) {
                                    return false;
                                }
                            }
                        } else if (numParent > 1) {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean nodeEquivalence(int taskA, int taskB) {
        for (int i = 0; i < _dependencies.length; i++) {
            if (_dependencies[taskA][i] == 1) {
                if (_dependencies[taskB][i] != 1) {
                    return false;
                }
                if (_communicationCosts[i][taskA] != _communicationCosts[i][taskB]) {
                    return false;
                }
            }
            if (_dependencies[i][taskA] == 1) {
                if (_dependencies[i][taskB] != 1) {
                    return false;
                }
                if (_communicationCosts[taskA][i] != _communicationCosts[taskB][i]) {
                    return false;
                }
            }
        }
        return true;
    }
}
