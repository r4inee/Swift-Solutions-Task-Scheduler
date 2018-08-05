package swiftsolutions.taskscheduler.branchandbound;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that executes the Branch and bound algorithm
 */
public class BNBAlgorithm implements Algorithm {

    private int _numProcessors;
    private BNBSchedule _optimalSchedule;
    private int _bound;
    private Set<BNBSchedule> _seenSchedules;
    private Map<Integer, Task> _taskMap;

    public BNBAlgorithm() {
        _seenSchedules = new HashSet<>();
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

        // Find the leaf nodes.
        Set<Task> leafs = tasks.values()
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
        dfs(convertedTasks, _bound, new BNBSchedule(convertedTasks.size(), _numProcessors));

        return convertSchedule(_optimalSchedule);
    }

    /**
     * Takes a BNBSchedule and returns it as a valid Schedule
     */
    private Schedule convertSchedule(BNBSchedule bnbSchedule) {
        Map<Integer, Pair<Integer, Integer>> taskMaps = new LinkedHashMap<>();
        int[][] arraySchedule = bnbSchedule._schedule;

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
    private void dfs(HashMap<Integer, BNBTask> tasks, long upperBound, BNBSchedule schedule) {

        // If the lower bound of the current schedule is larger than the upper bound, return;
        if (lowerBound(schedule) >= upperBound) {
            return;
        }

        // If we've seen a similar schedule return, if not add it.
        if (_seenSchedules.contains(schedule)) {
            return;
        } else {
            _seenSchedules.add(schedule);
        }

        // If we've finished then we update the optimal schedule and bound if the new schedule is better.
        if (tasks.isEmpty()) {
            long candidateUpperBound = schedule.getCost();
            if (candidateUpperBound <= upperBound) {
                _optimalSchedule = schedule;
                _bound = (int)candidateUpperBound;
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
            for (BNBTask availableTask: availableTasks) {
                //Create clones of the schedule and tasks
                BNBSchedule clonedSchedule = schedule.copy();
                HashMap<Integer, BNBTask> clonedTasks = new HashMap<>();
                tasks.forEach((Integer integer, BNBTask task) -> clonedTasks.put(integer, task.copy()));
                // Schedule the task
                scheduleTask(clonedTasks.get(availableTask._id), clonedTasks);
                clonedSchedule.addTask(availableTask, i);
                // Remove the task from the unscheduled tasks
                clonedTasks.remove(availableTask._id);
                // Recursive call on new schedule
                dfs(clonedTasks, _bound, clonedSchedule);
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
}

