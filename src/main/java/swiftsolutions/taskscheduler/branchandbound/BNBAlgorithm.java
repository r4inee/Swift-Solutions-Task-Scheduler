package swiftsolutions.taskscheduler.branchandbound;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Processor;
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

    public BNBAlgorithm() {
        _cloner = new Cloner();
    }

    @Override
    public void setProcessors(int processors) {
        _numProcessors = processors;
    }

    @Override
    public Schedule execute(Set<Task> tasks) {
        // Make a fake "rootTask" and get initial upperBound
        Set<BNBTask> convertedTasks = convertTasks(tasks);

        for (BNBTask task : convertedTasks) {
            _bound += task._procTime;
        }
        dfs(convertedTasks, _bound, new BNBSchedule(convertedTasks.size(), _numProcessors));
        return convertSchedule(_optimalSchedule);
    }

    private Schedule convertSchedule(BNBSchedule bnbSchedule) {
        Map<Task, Pair<Long, Long>>[] taskMaps = new Map[_numProcessors];
        int[][] arraySchedule = bnbSchedule._schedule;

        for (int i = 0; i < taskMaps.length; i++) {
            taskMaps[i] = new HashMap<>();
        }

        for (int i = 0; i < arraySchedule.length; i++) {
            long finishTime = arraySchedule[i][1];
            long startTime = arraySchedule[i][0];
            long procTime = finishTime - startTime;
            Pair<Long, Long> startEndTimes = new Pair<>(startTime, finishTime);
            Task task = new Task(i , (int)procTime);
            taskMaps[arraySchedule[i][2]].put(task, startEndTimes);
        }

        Processor[] processors = new Processor[taskMaps.length];

        for (int i = 0; i < taskMaps.length; i++) {
            processors[i] = new Processor(taskMaps[i], bnbSchedule._procEndTimes[i]);
        }

        return new Schedule(processors);
    }

    public Set<BNBTask> convertTasks(Set<Task> origTasks) {
        Set<BNBTask> tasks = new HashSet<>();
        int min =origTasks.stream().mapToInt((Task task) -> task.getTaskID()).min().getAsInt();
        origTasks.forEach((Task task) -> task.offsetId(min));
        for (Task task : origTasks) {
            tasks.add(new BNBTask(task));
        }
        return tasks;
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
        Set<BNBTask> availableTasks = getAvailableTasks(tasks);
        if (lowerBound(schedule, tasks) >= upperBound) {
            return;
        }

        long candidateUpperBound = schedule.getCost();

        if (tasks.isEmpty()) {
            if (candidateUpperBound <= upperBound) {
                _optimalSchedule = schedule;
                _bound = (int)candidateUpperBound;
            }
            return;
        }

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
        long idle = schedule.getIdleTime();
        long count = 0;
        for (BNBTask task : tasks) {
            count += task._procTime;
        }
        return (count - idle)/_numProcessors;
    }
}

