package swiftsolutions.taskscheduler.branchandbound;

import swiftsolutions.taskscheduler.Schedule;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class BNBSchedule implements Serializable{

    int _numTasks;
    int[][] _schedule;
    int[] _procEndTimes;

    public BNBSchedule(int numTasks, int numProc) {
        _numTasks = numTasks;
        // nodeId[start, end, proc]
        _schedule = new int[_numTasks][3];
        for (int i = 0; i < _schedule.length; i++) {
            for (int j = 0; j < _schedule[0].length; j++) {
                _schedule[i][j] = _schedule[i][j] = -1;
            }
        }
        _procEndTimes = new int[numProc];
    }

    public BNBSchedule(int numTasks, int[][] schedule, int[] procEndTimes) {
        _numTasks = numTasks;
        _schedule = schedule;
        _procEndTimes = procEndTimes;
    }

    public BNBSchedule copy() {
        int[][] scheduleCopy = new int[_schedule.length][_schedule[0].length];
        for (int i = 0; i < _schedule.length; i++) {
            for (int j = 0; j < _schedule[0].length; j++) {
                scheduleCopy[i][j] = _schedule[i][j];
            }
        }
        return new BNBSchedule(_numTasks, scheduleCopy, Arrays.copyOf(_procEndTimes, _procEndTimes.length));
    }

    public void addTask(BNBTask task, int proc) {
        int offset = 0;
        for (int parent : task._parents) {
            int tmpOffset = _schedule[parent][1];
            if (proc != _schedule[parent][2]) {
                tmpOffset += task._commCost[parent];
            }
            if (tmpOffset > offset) {
                offset = tmpOffset;
            }
        }

        int taskStart;
        if (offset < _procEndTimes[proc]) {
            taskStart = _procEndTimes[proc];
        } else {
            taskStart = offset;
        }
        _procEndTimes[proc] = taskStart + task._procTime;
        _schedule[task._id][0] = taskStart;
        _schedule[task._id][1] = _procEndTimes[proc];
        _schedule[task._id][2] = proc;
    }


    public int getCost() {
        int max = 0;
        for (int procCost : _procEndTimes) {
            if (procCost > max) {
                max = procCost;
            }
        }
        return max;
    }

    public int getIdleTime() {
        int[] procEndTimes = Arrays.copyOf(_procEndTimes, _procEndTimes.length);
        for (int[] task : _schedule) {
            if (task[0] != -1) {
                procEndTimes[task[2]] -= task[1] - task[0];
            }
        }
        int count = 0;
        for (int time : procEndTimes) {
            count += time;
        }
        return count;
    }

    @Override
    public int hashCode() {
        Set<Stack<Integer>> schedule = new HashSet<>();
        Stack<Integer>[] stacks = new Stack[_procEndTimes.length];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = new Stack<>();
        }
        for (int i = 0; i < _schedule.length; i++) {
            if (_schedule[i][0] != -1) {
                stacks[_schedule[i][2]].add(i);
                stacks[_schedule[i][2]].add(_schedule[i][0]);
            }
        }

        for(Stack<Integer> stack : stacks) {
            schedule.add(stack);
        }
        return schedule.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        BNBSchedule other = (BNBSchedule)obj;

        return other.hashCode() == hashCode();
    }
}
