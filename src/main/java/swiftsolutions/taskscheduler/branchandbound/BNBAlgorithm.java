package swiftsolutions.taskscheduler.branchandbound;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Processor;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Cloner;
import swiftsolutions.util.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BNBAlgorithm implements Algorithm {

    private int _numProcessors;
    private Cloner _cloner;
    private Schedule _optimalSchedule;
    private int _bound;

    public BNBAlgorithm() {
        _cloner = new Cloner();
    }

    @Override
    public void setProcessors(int processors) {
        _numProcessors = processors;
        _optimalSchedule = new Schedule(_numProcessors);
    }

    @Override
    public Schedule execute(Set<Task> tasks) {
        // Make a fake "rootTask" and get initial upperBound
        for (Task task : tasks) {
            _bound += task.getProcessTime();
        }
        dfs(tasks, _bound, new Schedule(_numProcessors));
        return _optimalSchedule;
    }

    private Set<Task> getAvailableTasks(Set<Task> tasks) {
        Set<Task> availableTasks = new HashSet<>();
        tasks.forEach((Task task) -> {
            if (task.getNumDependency() <= 0) {
                availableTasks.add(task);
            }
        });
        return availableTasks;
    }

    private void dfs(Set<Task> tasks, long upperBound, Schedule schedule) {
        Set<Task> availableTasks = this.getAvailableTasks(tasks);
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
            for (Task task: availableTasks) {
                Schedule clonedSchedule = _cloner.copy(schedule);
                Set<Task> clonedTasks = _cloner.copy(tasks);
                for (Task clonedTask : clonedTasks) {
                    if (clonedTask.getTaskID() == task.getTaskID()) {
                        clonedTask.scheduleTask();
                    }
                }
                clonedTasks.remove(task);
                clonedSchedule.addTask(task, i);
                dfs(clonedTasks, _bound, clonedSchedule);
            }
        }

    }

    private long lowerBound(Schedule schedule, Set<Task> tasks) {
        long idle = schedule.getIdleTime();
        long count = 0;
        for (Task task : tasks) {
            count += task.getProcessTime();
        }
        return (count - idle)/_numProcessors;
    }
}

