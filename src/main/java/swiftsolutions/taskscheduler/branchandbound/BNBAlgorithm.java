package swiftsolutions.taskscheduler.branchandbound;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Processor;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Cloner;
import swiftsolutions.util.Pair;

import java.util.HashSet;
import java.util.Set;

public class BNBAlgorithm implements Algorithm {

    private int _numProcessors;
    private Cloner _cloner;
    private Schedule _optimalSchedule;

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
        int initialUpperBound = 0;
        for (Task task : tasks) {
            _optimalSchedule.addTask(task, 0);
            initialUpperBound += task.getProcessTime();
        }


        dfs(tasks, initialUpperBound, new Schedule(_numProcessors));
        for (Processor processor : _optimalSchedule.getProcessors()) {
            System.out.println("---------------");
            processor.getTaskList().forEach((Task task, Pair<Long, Long> pair) -> {
                System.out.println("ID: " + task.getTaskID() + " ProcTime: " + task.getProcessTime() + " Start: " + pair.a + " End: " + pair.b);
            });
        }
        return _optimalSchedule;
    }

    private Set<Task> getAvailableTasks(Set<Task> tasks) {
        Set<Task> availableTasks = new HashSet<>();
        tasks.forEach((Task task) -> {
            if (task.getNumDependency() == 0) {
                availableTasks.add(task);
            }
        });
        return availableTasks;
    }

    private long dfs(Set<Task> tasks, long upperBound, Schedule schedule) {
        Set<Task> availableTasks = this.getAvailableTasks(tasks);
        if (lowerBound(schedule, tasks) >= upperBound) {
            return upperBound;
        }

        long candidateUpperBound = schedule.getCost();
        if (tasks.isEmpty() && candidateUpperBound < upperBound) {
            _optimalSchedule = schedule;
            upperBound = candidateUpperBound;
            return upperBound;
        }

        for (int i = 0; i < _numProcessors; i++) {
            for (Task task: availableTasks) {
                Schedule clonedSchedule = _cloner.copy(schedule);
                Set<Task> clonedTasks = _cloner.copy(tasks);
                clonedTasks.remove(task);
                clonedSchedule.addTask(task, i);
                task.scheduleTask();
                upperBound = dfs(clonedTasks, upperBound, clonedSchedule);
            }
        }

        return upperBound;
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

