package swiftsolutions.taskscheduler.branchandboundastarparallel;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.taskscheduler.branchandboundastar.Cache;
import swiftsolutions.util.Pair;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

public class BBAAlgorithmParallel implements Algorithm {
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


    // parallelization
    ForkJoinPool _commonPool;


    public BBAAlgorithmParallel() {
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


        // for Fork/Join task execution
        // TODO: need to configure no. of physical processors available on machine manually,
        // TODO: or is it done automatically ??
        _commonPool = ForkJoinPool.commonPool();


        _taskMap = tasks;
        _B = 0; // Max int
        int maxBotLevel = 0;
        int idleTime = 0;


        // Calculates the Bottom Level for each task.
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
        // Initialise an empty schedule;
        for (int i = 0; i < initialSchedule.length; i++) {
            initialSchedule[i][PROCESSOR_INDEX] = EMPTY;
            initialSchedule[i][START_TIME] = EMPTY;
        }

        // Priority queue by bottom level which is also in topological order.
        Queue<Integer> initialBound = new PriorityQueue<>((o1, o2) -> {
            Integer blA = _tasks[o1][BOTTOM_LVL];
            Integer blB = _tasks[o2][BOTTOM_LVL];
            return blB.compareTo(blA);
        });

        // Finding the maximum bottle level for cost calculation
        for (int task : _taskMap.keySet()) {
            _B += _taskMap.get(task).getProcessTime();
            if (_taskMap.get(task).getBottomLevel() > maxBotLevel) {
                maxBotLevel = _taskMap.get(task).getBottomLevel();
            }
            initialBound.add(task);
        }

        // Node Equivalence Pruning Technique, setting of initial map
        _nodeEquivalence = new int[tasks.size()][tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = i; j < tasks.size(); j++) {
                nodeEquivalence(i, j);
            }
        }

        // Finding a suitable starting bound for the algorithm.
        _fSInit = Math.max(_B / _numProcessors, maxBotLevel);

        // Copy to primitive array for FTO.
        int[] freeTasks = new int[tasks.size()];
        for (int i = 0; i < freeTasks.length; i++) {
            freeTasks[i] = initialBound.poll();
        }
        // If the task are either all independent or in fixed task order, start the algorithm and will order in BBA.
        int[] fTask = free(initialSchedule, _tasks);
        if ((isAllIndependent(fTask)) || (fTask.length == (_tasks.length - 1))) {
            BBA(EMPTY, EMPTY, tasks.size(), 0, procEndTimes, _tasks, initialSchedule, idleTime, true);
        } else {
            if ((fTask.length == 1) && ((_taskMap.get(fTask[0]).getChildTasks().size() == (_tasks.length - 1)))) {
                BBA(EMPTY, EMPTY, fTask.length, 0, procEndTimes, _tasks, initialSchedule, idleTime, true);
            } else {
                // Complete an FTO to find initial bound.
                FTO(freeTasks,0, procEndTimes, _tasks, initialSchedule, idleTime);
                BBA(EMPTY, EMPTY, fTask.length, 0, procEndTimes, _tasks, initialSchedule, idleTime, false); //Call the recursion algorithm
            }
        }
        return convertSchedule(_bestFState);
    }



    /**
     * This class is a RecursiveAction (ForkJoinTask) and serves parallelize the task of choosing a free task to
     * allocate on each processor, by parallelizing the consideration of each processor to allocate on.
     *
     */

    private class RecursiveBBA extends RecursiveAction {

        private int _procID;
        private int _prevTask;
        private int _prevProc;
        private int _numFreeTasks;
        private int _depth;
        private int[] _clndProcEndTimes;
        private int[][] _clndTasks;
        private int[][] _clndS;
        private int _idleTime;
        private boolean _fto;


        private RecursiveBBA(int prevTask, int prevProc, int numFreeTasks,
                            int depth, int[] clndProcEndTimes, int[][] clndTasks, int[][] clndS, int idleTime, boolean fto) {

            _prevTask = prevTask;
            _prevProc = prevProc;
            _numFreeTasks = numFreeTasks;
            _depth = depth;
            _clndProcEndTimes = clndProcEndTimes;
            _clndTasks = clndTasks;
            _clndS = clndS;
            _idleTime = idleTime;
            _fto = fto;

        }



        @Override
        protected void compute() {

            //priority queue for tasks based on cost function
            int[] freeTasks = BBAAlgorithmParallel.this.free(_clndS, _clndTasks);

            if (freeTasks.length != 0) {

                if (BBAAlgorithmParallel.this.isAllIndependent(freeTasks)) {
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

                    //TODO: recursiveFTO doesn't make a difference ??
/*                   RecursiveFTO recursiveFTO = new RecursiveFTO(orderedTasks, 0, _clndProcEndTimes, _clndTasks, _clndS, _idleTime);
                    recursiveFTO.fork();
                    recursiveFTO.join();*/
                    BBAAlgorithmParallel.this.FTO(orderedTasks, 0, _clndProcEndTimes, _clndTasks, _clndS, _idleTime);
                    return;

                } else if ((isFTO(freeTasks, _clndS) && (freeTasks.length == _numFreeTasks)) || (_fto)) {

                    if (_fto) {
                        freeTasks = BBAAlgorithmParallel.this._taskMap.keySet().stream().mapToInt(Number::intValue).toArray();
                    }

                    Comparator<Integer> c = (o1, o2) -> {
                        Integer parentO1DRT = 0;
                        int o1Parents = BBAAlgorithmParallel.this._taskMap.get(o1).getParentTasks().size();
                        int o1Children = BBAAlgorithmParallel.this._taskMap.get(o1).getChildTasks().size();
                        Integer parentO2DRT = 0;
                        int o2Parents = BBAAlgorithmParallel.this._taskMap.get(o2).getParentTasks().size();
                        int o2Children = BBAAlgorithmParallel.this._taskMap.get(o2).getChildTasks().size();

                        if (o1Parents == 1) {
                            int parent = BBAAlgorithmParallel.this._taskMap.get(o1).getParentTasks().iterator().next();

                            parentO1DRT = _clndS[parent][END_TIME] + BBAAlgorithmParallel.this._communicationCosts[parent][o1];
                        }
                        if (o2Parents == 1) {
                            int parent = BBAAlgorithmParallel.this._taskMap.get(o2).getParentTasks().iterator().next();
                            parentO2DRT = _clndS[parent][END_TIME] + BBAAlgorithmParallel.this._communicationCosts[parent][o2];
                        }

                        if (parentO1DRT.intValue() == parentO2DRT.intValue()) {
                            Integer outO1 = 0;
                            Integer outO2 = 0;
                            if (o1Children == 1) {
                                int child = BBAAlgorithmParallel.this._taskMap.get(o1).getChildTasks().iterator().next();
                                outO1 = BBAAlgorithmParallel.this._communicationCosts[o1][child];
                            }
                            if (o2Children == 1) {
                                int child = BBAAlgorithmParallel.this._taskMap.get(o2).getChildTasks().iterator().next();
                                outO2 = BBAAlgorithmParallel.this._communicationCosts[o2][child];
                            }
                            return outO2.compareTo(outO1);
                        }

                        return parentO1DRT.compareTo(parentO2DRT);
                    };

                    Queue<Integer> queue = new PriorityQueue<>(freeTasks.length, c);
                    for (int j = 0; j < freeTasks.length; j++) {
                        queue.add(freeTasks[j]);
                    }

                    // Verification step that all nodes are still in non-decreasing out edge costs.
                    if (queue.size() > 1) {
                        int[] verify = new int[queue.size()];
                        boolean order = true;
                        for (int j = 0; j < verify.length; j++) {
                            if (j > 1) {
                                int o1 = verify[j - 1];
                                int o2 = verify[j];

                                int outO1 = 0;
                                int outO2 = 0;
                                if (BBAAlgorithmParallel.this._taskMap.get(o1).getChildTasks().size() == 1) {
                                    outO1 = BBAAlgorithmParallel.this._communicationCosts[o1][
                                            BBAAlgorithmParallel.this._taskMap.get(o1).getChildTasks().iterator().next()];
                                }
                                if (BBAAlgorithmParallel.this._taskMap.get(o2).getChildTasks().size() == 1) {
                                    outO2 = BBAAlgorithmParallel.this._communicationCosts[o2][
                                            BBAAlgorithmParallel.this._taskMap.get(o2).getChildTasks().iterator().next()];
                                }

                                if (outO2 < outO1) {
                                    order = false;
                                    break;
                                }
                            }
                        }

                        if (order) {
                            int[] orderedTasks = new int[freeTasks.length];
                            for (int i = 0; i < freeTasks.length; i++) {
                                orderedTasks[i] = queue.poll();
                            }
                            //TODO: recursiveFTO doesn't make a difference ??
                /*            RecursiveFTO recursiveFTO = new RecursiveFTO(orderedTasks, 0, _clndProcEndTimes, _clndTasks, _clndS, _idleTime);
                            recursiveFTO.fork();
                            recursiveFTO.join();*/
                            BBAAlgorithmParallel.this.FTO(orderedTasks, 0, _clndProcEndTimes, _clndTasks, _clndS, _idleTime);
                            return;
                        }
                    }

                }

             }




            Cache cache = new Cache(BBAAlgorithmParallel.this._numProcessors, _clndS);
            if (BBAAlgorithmParallel.this._seenSchedules.contains(cache)) {
                return; // prune
            } else {
                BBAAlgorithmParallel.this._seenSchedules.add(cache);
            }


            for (int i = 0; i < freeTasks.length; i++) {
                for (int j = 0; j < BBAAlgorithmParallel.this._numProcessors; j++) {

                    if (j > BBAAlgorithmParallel.this.getFirstEmptyProc(_clndProcEndTimes)) {
                        break;
                    }
                    // Partial Duplicate Detection
                    if (freeTasks.length == _numFreeTasks) {
                        if (j < _prevProc) {
                            continue;
                        }
                    }
                    int taskID = freeTasks[i];
                    // Node Equivalence
                    if (_prevTask != EMPTY) {
                        if (_nodeEquivalence[_prevTask][taskID] == 1) {
                            break;
                        }
                    }

                    RecursiveBBACore recursiveBBACore = new RecursiveBBACore(_clndProcEndTimes[j], freeTasks, taskID, j, _prevTask, _prevProc,
                            _numFreeTasks, _depth, _clndProcEndTimes, _clndTasks, _clndS, _idleTime);

                    recursiveBBACore.fork();

                }
            }

        }


    }







    /**
     * This class contains the core logic for the BBA* algorithm by allowing the free task allocation on each processor
     * to be parallelized.
     * */

    private class RecursiveBBACore extends RecursiveAction {

        private int _procEndTime;
        private int[] _freeTasks;
        private int _taskID;
        private int _procID;
        private int _prevTask;
        private int _prevProc;
        private int _numFreeTasks;
        private int _depth;
        private int[] _clndProcEndTimes;
        private int[][] _clndTasks;
        private int[][] _clndS;
        private int _idleTime;


        /**
         *
         * */
        private RecursiveBBACore(int procEndTime, int[] freeTasks, int taskID, int procID, int prevTask, int prevProc, int numFreeTasks,
                             int depth, int[] clndProcEndTimes, int[][] clndTasks, int[][] clndS, int idleTime) {

            _procEndTime = procEndTime;
            _freeTasks = freeTasks;
            _taskID = taskID;
            _procID = procID;
            _prevTask = prevTask;
            _prevProc = prevProc;
            _numFreeTasks = numFreeTasks;
            _depth = depth;
            _idleTime = idleTime;
            // cloned state
            _clndS = BBAAlgorithmParallel.this.copySchedule(clndS);
            _clndProcEndTimes = Arrays.copyOf(clndProcEndTimes, clndProcEndTimes.length);
            _clndTasks = BBAAlgorithmParallel.this.copyTasks(clndTasks);

        }


        @Override
        protected void compute() {

            // Calculate parent offset
            int offset = 0;
            for (int di = 0; di < BBAAlgorithmParallel.this._dependencies[_taskID].length; di++) {
                int tempOffset = _clndS[di][END_TIME];
                //look at all parents of current task (parent task id is DJ)
                if (BBAAlgorithmParallel.this._dependencies[_taskID][di] == 1) {
                    //check if that parent is on the same proc
                    if (_clndS[di][PROCESSOR_INDEX] != _procID) {
                        //if the processor is not on the same
                        tempOffset += BBAAlgorithmParallel.this._communicationCosts[di][_taskID];
                    }
                    if (tempOffset > offset) {
                        offset = tempOffset;
                    }
                }
            }

            // Calculate the start time of the task
            int taskStart;
            if (offset < _clndProcEndTimes[_procID]) {
                taskStart = _clndProcEndTimes[_procID];
            } else if (_clndProcEndTimes[_procID] == 0) {
                taskStart = offset;
                _idleTime = offset;
            } else {
                taskStart = offset;
                _idleTime += offset - _clndProcEndTimes[_procID];
            }

            // Allocate the task on the cloned schedule and update cloned dependencies and processor end times.
            _clndS[_taskID][PROCESSOR_INDEX] = _procID;
            _clndS[_taskID][START_TIME] = taskStart;
            _clndS[_taskID][END_TIME] = taskStart + _clndTasks[_taskID][PROC_TIME];
            _clndProcEndTimes[_procID] = _clndS[_taskID][END_TIME];
            for (int dj = 0; dj < _dependencies.length; dj++) {
                if (_dependencies[dj][_taskID] == 1) {
                    _clndTasks[dj][NUM_DEP]--;
                }
            }

            // Calculate the new current number of free tasks and increment depth
            _numFreeTasks = free(_clndS, _clndTasks).length - 1;
            _depth++;

            // if cost is lower than B(est) and depth is max, set current best, go back up tree
            if (cost(_clndS, _clndProcEndTimes, _taskID, offset, _idleTime) <= _B && (_depth == _tasks.length)) {
                _bestFState = _clndS; // clonedS
                _B = cost(_clndS, _clndProcEndTimes, _taskID, offset, _idleTime);
            }

            // if cost is lower than B(est) and depth is max, recursive call
            if (cost(_clndS, _clndProcEndTimes, _taskID, offset, _idleTime) <= _B && _depth < _tasks.length) {
                RecursiveBBA recursiveBBA = new RecursiveBBA(_taskID, _procID, _numFreeTasks, _depth, _clndProcEndTimes,
                        _clndTasks, _clndS, _idleTime, false);

                recursiveBBA.fork();
            }

            // Reset the offseted values.
            _numFreeTasks = _freeTasks.length;
            _depth--;
            if (offset > _procEndTime) {
                _idleTime -= offset - _procEndTime;
            }


        }
    }


    /**
     * This is the main method that creates the schedules implemented using the pseudo code of BBA*.
     * It uses the Fork/Join framework to parallelize the search through the solution space.
     *
     * @param previousTask The previous task which was scheduled
     * @param previousProcessor The previous processor which task was scheduled
     * @param numFreeTasks The previous number of available tasks
     * @param depth The current recursive depth
     */
    private void BBA(int previousTask, int previousProcessor, int numFreeTasks, int depth, int[] procEndTimes, int[][] tasks, int[][] s,
                     int idleTime, boolean fto) {


        RecursiveBBA recursiveBBA = new RecursiveBBA(previousTask, previousProcessor, numFreeTasks, depth,
                procEndTimes, tasks, s, idleTime, fto);

        _commonPool.invoke(recursiveBBA);

    }


    /**
     * Recursive Ordering for FTO, AllIndependence and InitialBound.
     *
     * @param orderedTasks A primitive priority queue off ordered tasks
     * @param procEndTimes Index pointing to element in the priority queue for current state
     * @param tasks Reference to the map of tasks
     * @param s The current schedule
     * @param idleTime The cumulative idle time
     */
    private void FTO(int[] orderedTasks, int index, int[] procEndTimes, int[][] tasks, int[][] s, int idleTime) {
        int task = orderedTasks[index];

        // Round robin technique to cover all permutaions of processors
        for (int j = 0; j < _numProcessors; j++) {
            if (j > getFirstEmptyProc(procEndTimes)) {
                break;
            }

            // Cloning of schedules, processor end times and tasks
            int[][] clonedS = copySchedule(s);
            int[] clonedProcEndTimes = Arrays.copyOf(procEndTimes, procEndTimes.length); //copy Processor end times
            int[][] clonedTasks = copyTasks(tasks);

            // Calculate parent offset
            int offset = 0;
            for (int di = 0; di < _dependencies[task].length; di++) {
                int tempOffset = clonedS[di][END_TIME];
                //look at all parents of current task (parent task id is DJ)
                if (_dependencies[task][di] == 1) {
                    //check if that parent is on the same proc
                    if (clonedS[di][PROCESSOR_INDEX] != j) {
                        //if the processor is not on the same
                        tempOffset += _communicationCosts[di][task];
                    }
                    if (tempOffset > offset) {
                        offset = tempOffset;
                    }
                }
            }

            // Calculate the start time of the task
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

            // Allocate the task on the cloned schedule and update cloned dependencies and processor end times.
            clonedS[task][PROCESSOR_INDEX] = j;
            clonedS[task][START_TIME] = taskStart;
            clonedS[task][END_TIME] = taskStart + clonedTasks[task][PROC_TIME];
            clonedProcEndTimes[j] = clonedS[task][END_TIME];
            for (int dj = 0; dj < _dependencies.length; dj++) {
                if (_dependencies[dj][task] == 1) {
                    clonedTasks[dj][NUM_DEP]--;
                }
            }

            // Check if all the tasks has been ordered, FTO guarantees optimality.
            if (index == (orderedTasks.length-1)) {
                // Update the current bound.
                int bound = cost(clonedS, clonedProcEndTimes, task, offset, idleTime);
                if (bound <= _B) {
                    _bestFState = clonedS; // clonedS
                    _B = bound;
                }
            } else {
                // Recursive call to schedule next task
                FTO(orderedTasks, index+1, clonedProcEndTimes, clonedTasks, clonedS, idleTime);
            }

            // Reset the offseted values.
            if (offset > procEndTimes[j]) {
                idleTime -= offset - procEndTimes[j];
            }
        }
    }


    //TODO: this class may not be necessary
    private class RecursiveFTO extends RecursiveAction {

        private int[] _orderedTasks;
        private int _index;
        private int[] _procEndTimes;
        private int[][] _tasks;
        private int[][] _s;
        private int _idleTime;


        private RecursiveFTO(int[] orderedTasks, int index, int[] procEndTimes, int[][] tasks, int[][] s, int idleTime) {
            _orderedTasks = orderedTasks;
            _index = index;
            _procEndTimes = procEndTimes;
            _tasks = tasks;
            _s = s;
            _idleTime = idleTime;
        }

        @Override
        protected void compute() {


            int task = _orderedTasks[_index];

            // Round robin technique to cover all permutaions of processors
            for (int j = 0; j < _numProcessors; j++) {
                if (j > getFirstEmptyProc(_procEndTimes)) {
                    break;
                }

                // Cloning of schedules, processor end times and tasks
                int[][] clonedS = copySchedule(_s);
                int[] clonedProcEndTimes = Arrays.copyOf(_procEndTimes, _procEndTimes.length); //copy Processor end times
                int[][] clonedTasks = copyTasks(_tasks);

                // Calculate parent offset
                int offset = 0;
                for (int di = 0; di < _dependencies[task].length; di++) {
                    int tempOffset = clonedS[di][END_TIME];
                    //look at all parents of current task (parent task id is DJ)
                    if (_dependencies[task][di] == 1) {
                        //check if that parent is on the same proc
                        if (clonedS[di][PROCESSOR_INDEX] != j) {
                            //if the processor is not on the same
                            tempOffset += _communicationCosts[di][task];
                        }
                        if (tempOffset > offset) {
                            offset = tempOffset;
                        }
                    }
                }

                // Calculate the start time of the task
                int taskStart;
                if (offset < clonedProcEndTimes[j]) {
                    taskStart = clonedProcEndTimes[j];
                } else if (clonedProcEndTimes[j] == 0) {
                    taskStart = offset;
                    _idleTime = offset;
                } else {
                    taskStart = offset;
                    _idleTime += offset - clonedProcEndTimes[j];
                }

                // Allocate the task on the cloned schedule and update cloned dependencies and processor end times.
                clonedS[task][PROCESSOR_INDEX] = j;
                clonedS[task][START_TIME] = taskStart;
                clonedS[task][END_TIME] = taskStart + clonedTasks[task][PROC_TIME];
                clonedProcEndTimes[j] = clonedS[task][END_TIME];
                for (int dj = 0; dj < _dependencies.length; dj++) {
                    if (_dependencies[dj][task] == 1) {
                        clonedTasks[dj][NUM_DEP]--;
                    }
                }

                // Check if all the tasks has been ordered, FTO guarantees optimality.
                if (_index == (_orderedTasks.length-1)) {
                    // Update the current bound.
                    int bound = cost(clonedS, clonedProcEndTimes, task, offset, _idleTime);
                    if (bound <= _B) {
                        _bestFState = clonedS; // clonedS
                        _B = bound;
                    }
                } else {

                    RecursiveFTO recursiveFTO = new RecursiveFTO(_orderedTasks, _index+1, clonedProcEndTimes, clonedTasks, clonedS, _idleTime);
                    recursiveFTO.fork();
                    this.compute();

                    // Recursive call to schedule next task
                    //FTO(_orderedTasks, _index+1, clonedProcEndTimes, clonedTasks, clonedS, _idleTime);
                }

                // Reset the offseted values.
                if (offset > _procEndTimes[j]) {
                    _idleTime -= offset - _procEndTimes[j];
                }
            }

        }

    }




    /**
     * Method to make a copy of a schedule.
     *
     * @param s The schedule to be copied.
     * @return A copy of the schedule
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
     * Method to make a copy of tasks.
     *
     * @param tasks The set of tasks to be copied
     * @return A copy of the tasks map
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
     * Recursive function that updates the bottom levels of the nodes in the input set and all the nodes of that parent.
     *
     * @param nodes The set of tasks
     * @param currentBottomLevel The current bottom level calculated
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
     * Based off the cost function f described where f = max{Initial(s), idle-time(s), bottom-level(s), DRT(s)}.
     *
     * @param s The current complete schedule
     * @param procEndTimes The completed processor end times
     * @param taskID The last task to be scheduled
     * @param dataReadyTime Communication delay from parents offsets
     * @param idleTime The cumulative idle time
     * @return The total cost of the input schedule
     */
    private int cost(int[][] s, int[] procEndTimes, int taskID, int dataReadyTime, int idleTime) {
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
     * This method takes in a schedule and returns an array of free tasks which are tasks that are not already
     * scheduled and there parents have all been scheduled.
     *
     * @param s The current schedule
     * @param tasks The map of tasks relationships
     * @return The current list of available tasks
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
     * This method is used to convertTasks the task map passed into execute() into the arrays used to map the data in
     * this class
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
     * Convert the final 2D array schedule back into a schedule that is to be returned be the execute method.
     *
     * @param schedule The current schedule outputted by the algorithm
     * @return The converted Schedule object for output parser
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

    /**
     * Find the first empty processor from the processor end times.
     *
     * @param procEndTimes Contains the finishing time of each processor at current state of algorithm
     * @return The id of the first empty processor
     */
    private int getFirstEmptyProc(int[] procEndTimes) {
        for (int i = 0; i < _numProcessors; i++) {
            if (procEndTimes[i] == 0) {
                return i;
            }
        }
        return _numProcessors;
    }

    /**
     * Checks if all the left over tasks are independent from each other.
     *
     * @param freeTask The list of available tasks at current iteration of the algorithm
     * @return true if the available tasks are all independent, else false
     */
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

    /**
     * Checks if the available tasks can be scheduled in a fixed order.
     * @param freeTasks The list of available tasks at the current iteration of the algorithm
     * @param schedule The current schedule
     * @return true if the available tasks can be order it FTO, else false
     */
    private boolean isFTO(int[] freeTasks, int[][] schedule) {
        for (int i = 0; i < freeTasks.length; i++) {
            int parents = _taskMap.get(freeTasks[i]).getParentTasks().size();
            int children = _taskMap.get(freeTasks[i]).getChildTasks().size();

            // Check if all nodes has only one or less parent and one or less child for detecting Fork+Join FTO
            if ((parents <= 1) && (children <= 1)) {
                if ((parents == 0) && (children == 0)) {
                    continue;
                }

                // Check all nodes have the same children for detecting Join FTO.
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

                // Check all nodes have the same parent for detecting Fork FTO.
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

    /**
     * Pre-processing of the algorithm to mark all tasks which are equivalent to each other, same parent, child,
     * weight and also communication costs.
     *
     * @param taskA Task A
     * @param taskB Task B
     */
    private void nodeEquivalence(int taskA, int taskB) {
        for (int i = 0; i < _dependencies.length; i++) {
            if (_dependencies[taskA][i] == 1) {
                if (_dependencies[taskB][i] != 1) {
                    return;
                }
                if (_communicationCosts[i][taskA] != _communicationCosts[i][taskB]) {
                    return;
                }
            }
            if (_dependencies[i][taskA] == 1) {
                if (_dependencies[i][taskB] != 1) {
                    return;
                }
                if (_communicationCosts[taskA][i] != _communicationCosts[taskB][i]) {
                    return;
                }
            }
        }
        _nodeEquivalence[taskA][taskB] = 1;
    }
}

