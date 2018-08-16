package swiftsolutions.taskscheduler.branchandbound;

import swiftsolutions.interfaces.taskscheduler.VisualAlgorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that executes the Branch and bound algorithm
 */
public class BNBAlgorithmVisual extends VisualAlgorithm {

    private int _numProcessors;
    private volatile BNBSchedule _optimalSchedule;
    private int _bound;
    private Set<BNBSchedule> _seenSchedules;
    private Map<Integer, Task> _taskMap;
    private volatile int _branches;
    private volatile int _validSchedules;
    private volatile int _pruned;
    private volatile boolean _done;

    public BNBAlgorithmVisual() {
        _seenSchedules = new HashSet<>();
        _branches = 0;
        _pruned = 0;
        _done = false;
    }

    /**
     * See Algorithm#execute()
     * @param processors
     */
    @Override
    public void setProcessors(int processors) {
        _numProcessors = processors;
    }

    /**
     * See Algorithm#execute()
     */
    @Override
    public Schedule execute(Map<Integer, Task> tasks) {
        _taskMap = tasks;

        return null;
    }

    /**
     * Takes a BNBSchedule and returns it as a valid Schedule
     */
    @Override
    public Schedule getFinishedSchedule() {
        Map<Integer, Pair<Integer, Integer>> taskMaps = new LinkedHashMap<>();
        int[][] arraySchedule = _optimalSchedule._schedule;

        // Convert schedule to taskMaps
        for (int i = 0; i < arraySchedule.length; i++) {
            int startTime = arraySchedule[i][BNBSchedule.START_TIME];
            int processor = arraySchedule[i][BNBSchedule.PROCCESSOR_INDEX];
            taskMaps.put(i, new Pair<>(processor, startTime));
        }

        return new Schedule(taskMaps, arraySchedule.length);
    }

    /**
     * Convert Tasks to BNBTasks
     */
    public HashMap<Integer, BNBTask> convertTasks() {
        HashMap<Integer, BNBTask> tasks = new HashMap<>();
        _taskMap.forEach((Integer integer, Task task) -> tasks.put(integer, new BNBTask(task, integer)));
        return tasks;
    }

    /**
     * Recursive function to get bottom levels
     * @param nodes nodes where all the children nodes bottom levels have already been calculated
     * @param currBottomLevel the current path length
     */
    private void getBottomLevels(Set<Integer> nodes, int currBottomLevel) {
        for (Integer node: nodes) {
            // Update current nodes bottom level
            _taskMap.get(node).updateBottomLevel(currBottomLevel + _taskMap.get(node).getProcessTime());

            // For each parent run the function with the new currBottomLevel
            if (!_taskMap.get(node).getParentTasks().isEmpty()) {
                getBottomLevels(_taskMap.get(node).getParentTasks(),
                        currBottomLevel + _taskMap.get(node).getProcessTime());
            }
        }
    }

    /**
     * Recursive depth-first search branch and bound algorithm.
     * @param tasks tasks that have not been scheduled
     * @param upperBound the current best schedule founds cost function
     * @param schedule the current schedule
     */
    private void dfs(HashMap<Integer, BNBTask> tasks, long upperBound, BNBSchedule schedule, Queue<BNBTask> fto,
                     Set<BNBTask> free, int lastProc) {

        _branches++;

        // Algorithm has been manually stopped
        if (isInterrupted()) {
            return;
        }

        if (lowerBound(schedule) >= upperBound) {
            return;
        }

        // If we've seen a similar schedule return, if not add it.
        if (_seenSchedules.contains(schedule)) {
            _pruned++;
            return;
        } else {
            _seenSchedules.add(schedule);
        }

        // If we've finished then we update the optimal schedule and bound if the new schedule is better.
        if (tasks.isEmpty()) {
            long candidateUpperBound = schedule.getCost();
            if (candidateUpperBound <= upperBound) {
                _validSchedules++;
                _optimalSchedule = schedule;
                _bound = (int) candidateUpperBound;
            }
            return;
        }

        // Get the tasks with no dependencies unscheduled
        Set<BNBTask> availableTasks = tasks.values()
                .stream()
                .filter((BNBTask task) -> task._numDependency == 0)
                .collect(Collectors.toSet());

        // For try schedule each available task to each schedule
        for (int i = 0; i < _numProcessors; i++) {
            if ((fto != null) && (fto.size() == availableTasks.size() && (!fto.isEmpty()))) {
                // When the schedule is in FTO automatically poll for the next task in the queue and schedule it.
                BNBTask task = fto.poll();
                BNBSchedule clonedSchedule = schedule.copy();
                HashMap<Integer, BNBTask> clonedTasks = new HashMap<>();
                tasks.forEach((Integer integer, BNBTask t) -> clonedTasks.put(integer, t.copy()));
                // Schedule the task
                scheduleTask(clonedTasks.get(task._id), clonedTasks);
                clonedSchedule.addTask(task, i);
                // Remove the task from the unscheduled tasks
                clonedTasks.remove(task._id);
                dfs(clonedTasks, _bound, clonedSchedule, fto, availableTasks, i);
            } else if ((availableTasks.size() == 1) && (availableTasks.iterator().next()._children.length == tasks.size()-1)) {
                // If all there is a single task, it is default to fork.
                BNBTask task = availableTasks.iterator().next();
                BNBSchedule clonedSchedule = schedule.copy();
                HashMap<Integer, BNBTask> clonedTasks = new HashMap<>();
                tasks.forEach((Integer integer, BNBTask t) -> clonedTasks.put(integer, t.copy()));
                // Schedule the task
                scheduleTask(clonedTasks.get(task._id), clonedTasks);
                clonedSchedule.addTask(task, i);
                // Remove the task from the unscheduled tasks
                clonedTasks.remove(task._id);
                // Recursive call on new schedule
                if (task._children.length == 0) {
                    dfs(clonedTasks, _bound, clonedSchedule, null, availableTasks, i);
                    return;
                }
                // The queue is ordered in increasing outgoing edge weight.
                Queue<BNBTask> queue = new PriorityQueue<>((o1, o2) -> {
                    Integer i1 = o1._commCost[task._id];
                    Integer i2 = o2._commCost[task._id];
                    return i1.compareTo(i2);
                });
                for (int j = 0; j < task._children.length; j++) {
                    queue.add(tasks.get(task._children[j]));
                }
                dfs(clonedTasks, _bound, clonedSchedule, queue, availableTasks, i);
                return;
            } else {
                // Check if it is a joinFTO by checking if every available task share common children.
                Set<Integer> constrainedTask = new HashSet<>();
                BNBTask firstAvailable = availableTasks.iterator().next();
                for (int j = 0; j < firstAvailable._children.length; j++) {
                    constrainedTask.add(firstAvailable._children[j]);
                }
                for (BNBTask t : availableTasks) {
                    Set<Integer> foundTask = new HashSet<>();
                    for (int j = 0; j < t._children.length; j++) {
                        foundTask.add(t._children[j]);
                    }
                    constrainedTask.retainAll(foundTask);
                }

                // If this if statement is true, then it is a joinFTO
                if (constrainedTask.size() > 0) {
                    Set<BNBTask> children = new HashSet<>();
                    for (Integer id : constrainedTask) {
                        children.add(tasks.get(id));
                    }
                    // The queue is ordered in decreasing outgoing edge weight.
                    Queue<BNBTask> queue = new PriorityQueue<>((o1, o2) -> {
                        Integer i1 = 0;
                        Integer i2 = 0;
                        for (BNBTask c : children) {
                            i1 += c._commCost[o1._id];
                            i2 += c._commCost[o2._id];
                        }
                        return i2.compareTo(i1);
                    });
                    queue.addAll(availableTasks);
                    // Schedule th next task for FTO
                    BNBTask scheduleTask = queue.poll();
                    BNBSchedule clonedSchedule = schedule.copy();
                    HashMap<Integer, BNBTask> clonedTasks = new HashMap<>();
                    tasks.forEach((Integer integer, BNBTask t) -> clonedTasks.put(integer, t.copy()));
                    // Schedule the task
                    scheduleTask(clonedTasks.get(scheduleTask._id), clonedTasks);
                    clonedSchedule.addTask(scheduleTask, i);
                    // Remove the task from the unscheduled tasks
                    clonedTasks.remove(scheduleTask._id);
                    dfs(clonedTasks, _bound, clonedSchedule, queue, availableTasks, i);
                } else {
                    // Processor normalisation
                    if (i > schedule.getFirstEmptyProc()) {
                        break;
                    }
                    for (BNBTask availableTask : availableTasks) {
                        // Partial Duplicate Avoidance
                        if (!free.contains(availableTask)) {
                            if (i < lastProc) {
                                continue;
                            }
                        }
                        // Create clones of the schedule and tasks
                        BNBSchedule clonedSchedule = schedule.copy();
                        HashMap<Integer, BNBTask> clonedTasks = new HashMap<>();
                        tasks.forEach((Integer integer, BNBTask task) -> clonedTasks.put(integer, task.copy()));
                        // Schedule the task
                        scheduleTask(clonedTasks.get(availableTask._id), clonedTasks);
                        clonedSchedule.addTask(availableTask, i);
                        // Remove the task from the unscheduled tasks
                        clonedTasks.remove(availableTask._id);
                        // Recursive call on new schedule
                        dfs(clonedTasks, _bound, clonedSchedule, null, availableTasks, i);
                    }
                }
            }
        }
    }

    /**
     * Notify children that its parent has been scheduled
     * @param task task being scheduled
     * @param tasks map of unscheduled tasks
     */
    private void scheduleTask(BNBTask task, HashMap<Integer, BNBTask> tasks) {
        for (int i : task._children) {
            if (tasks.get(i) != null) {
                tasks.get(i)._numDependency--;
            }
        }
    }

    /**
     * Get an estimate for the lower bound cost of a schedule
     * @param schedule the schedule that we want the lower bound of.
     * @return the lower bound of the schedule.
     */
    private long lowerBound(BNBSchedule schedule) {
        // Get the maximum of taskStarTime + bottomLevel from each node in the schedule
        long lowerBound = 0;

        for (int i = 0; i < schedule._schedule.length; i++) {
            if (schedule._schedule[i][BNBSchedule.START_TIME] != -1) {
                if (schedule._schedule[i][BNBSchedule.START_TIME] + _taskMap.get(i).getBottomLevel() > lowerBound) {
                    lowerBound = schedule._schedule[i][BNBSchedule.START_TIME] + _taskMap.get(i).getBottomLevel();
                }
            }
        }
        return lowerBound;
    }

    /**
     * Starts the algorithm
     */
    @Override
    public void run() {
        super.run();
        // Find the leaf nodes.
        Set<Task> leafs = _taskMap.values()
                .stream()
                .filter((Task task) -> task.getChildTasks().size() == 0)
                .collect(Collectors.toSet());

        // Use leaf nodes to find the bottom levels of all the tasks
        for (Task leaf: leafs) {
            leaf.updateBottomLevel(leaf.getProcessTime());
            getBottomLevels(leaf.getParentTasks(), leaf.getProcessTime());
        }

        // Convert the tasks to BNB Tasks
        HashMap<Integer, BNBTask> convertedTasks = convertTasks();

        // Set an initial bound
        _bound = Integer.MAX_VALUE;

        // Star the algorithm
        dfs(convertedTasks, _bound, new BNBSchedule(convertedTasks.size(), _numProcessors), null, new HashSet<>(), -1);
        _done = true;
    }


    /**
     * See VisualAlgorithm#getPruned()
     * @return
     */
    @Override
    public int getPruned() {
        return _pruned;
    }

    /**
     * See VisualAlgorithm#getBranches()
     * @return
     */
    @Override
    public int getBranches() {
        return this._branches;
    }

    /**
     * See VisualAlgorithm#getSchedule()
     * @return
     */
    @Override
    public int[][] getSchedule() {
        return _optimalSchedule == null ? null : _optimalSchedule._schedule;
    }

    /**
     * See VisualAlgorithm#getProcessors()
     * @return
     */
    @Override
    public int getProcessors() {
        return _numProcessors;
    }

    /**
     * See VisualAlgorithm#getValidSchedules()
     * @return
     */
    @Override
    public int getValidSchedules() {
        return _validSchedules;
    }

    /**
     * See VisualAlgorithm#getUpperbound()
     * @return
     */
    @Override
    public int getUpperbound() {
        return _bound;
    }

    /**
     * See VisualAlgorithm#isDone()
     * @return
     */
    @Override
    public boolean isDone() {
        return _done;
    }



}

