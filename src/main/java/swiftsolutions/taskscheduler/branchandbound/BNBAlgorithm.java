package swiftsolutions.taskscheduler.branchandbound;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Cloner;
import swiftsolutions.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class BNBAlgorithm implements Algorithm {

    private int _numProcessors;
    private Cloner _cloner;
    private BNBSchedule _optimalSchedule;
    private int _bound;
    private Set<BNBSchedule> _seenSchedules;
    private Map<Integer, Task> _taskMap;

    public BNBAlgorithm() {
        _cloner = new Cloner();
        _seenSchedules = new HashSet<>();
    }

    @Override
    public void setProcessors(int processors) {
        _numProcessors = processors;
    }

    @Override
    public Schedule execute(Map<Integer, Task> tasks) {
        _taskMap = tasks;
        Set<Task> leafs = tasks.values()
                .stream()
                .filter((Task task) -> task.getChildTasks().size() == 0)
                .collect(Collectors.toSet());

        for (Task leaf: leafs) {
            leaf.updateBottomLevel(leaf.getProcessTime());
            getBottomLevels(leaf.getParentTasks(), leaf.getProcessTime());
        }
        Set<BNBTask> convertedTasks = convertTasks();


        for (BNBTask task : convertedTasks) {
            _bound += task._procTime;
        }
        dfs(convertedTasks, _bound, new BNBSchedule(convertedTasks.size(), _numProcessors));
        return convertSchedule(_optimalSchedule);
    }

    private Schedule convertSchedule(BNBSchedule bnbSchedule) {
        // [task_id -> (processorID, startTime)]
        Map<Integer, Pair<Integer, Integer>> taskMaps = new LinkedHashMap<>();
        int[][] arraySchedule = bnbSchedule._schedule;

        for (int i = 0; i < arraySchedule.length; i++) {
            int startTime = arraySchedule[i][0];
            int processor = arraySchedule[i][2];
            taskMaps.put(i, new Pair<>(processor, startTime));
        }

        return new Schedule(taskMaps, arraySchedule.length);
    }

    public Set<BNBTask> convertTasks() {
        Set<BNBTask> tasks = new HashSet<>();
        for (Integer id : _taskMap.keySet()) {
            tasks.add(new BNBTask(_taskMap.get(id), id));
        }
        return tasks;
    }

    private void getBottomLevels(Set<Integer> nodes, int currBottomLevel) {
        for (Integer node: nodes) {
            _taskMap.get(node).updateBottomLevel(currBottomLevel + _taskMap.get(node).getProcessTime());
            if (_taskMap.get(node).getParentTasks().size() != 0) {
                getBottomLevels(_taskMap.get(node).getParentTasks(),
                        currBottomLevel + _taskMap.get(node).getProcessTime());
            }
        }
    }

    private Set<BNBTask> getAvailableTasks(Set<BNBTask> tasks) {
        Set<BNBTask> availableTasks = new HashSet<>();
        tasks.forEach((BNBTask task) -> {
            if (task._numDependency <= 0) {
                availableTasks.add(task);
            }
        });
        return availableTasks;
    }

    private void dfs(Set<BNBTask> tasks, long upperBound, BNBSchedule schedule) {
        if (lowerBound(schedule, tasks) >= upperBound) {
            return;
        }

        if (_seenSchedules.contains(schedule)) {
            return;
        } else {
            _seenSchedules.add(schedule);
        }

        long candidateUpperBound = schedule.getCost();

        if (tasks.isEmpty()) {
            if (candidateUpperBound <= upperBound) {
                _optimalSchedule = schedule;
                _bound = (int)candidateUpperBound;
            }
            return;
        }

        Set<BNBTask> availableTasks = getAvailableTasks(tasks);

        for (int i = 0; i < _numProcessors; i++) {
            for (BNBTask availableTask: availableTasks) {
                BNBSchedule clonedSchedule = schedule.copy();
                Set<BNBTask> clonedTasks = tasks.stream().map((BNBTask task) -> task.copy()).collect(Collectors.toSet());
                for (BNBTask clonedTask : clonedTasks) {
                    if (clonedTask._id == availableTask._id) {
                        scheduleTask(clonedTask, clonedTasks);
                        break;
                    }
                }
                clonedTasks.remove(availableTask);
                clonedSchedule.addTask(availableTask, i);
                dfs(clonedTasks, _bound, clonedSchedule);
            }
        }

    }

    private void scheduleTask(BNBTask task, Set<BNBTask> tasks) {
        for (int i : task._children) {
            for (BNBTask child : tasks) {
                if (child._id == i) {
                    child._numDependency--;
                }
            }
        }
    }


    private long lowerBound(BNBSchedule schedule, Set<BNBTask> tasks) {
        long lowerBound = 0;
        for (int i = 0; i < schedule._schedule.length; i++) {
            if (schedule._schedule[i][0] != -1) {
                if (schedule._schedule[i][0] + _taskMap.get(i).getBottomLevel() > lowerBound) {
                    lowerBound = schedule._schedule[i][0] + _taskMap.get(i).getBottomLevel();
                }
            }
        }
        return lowerBound;
    }
}

