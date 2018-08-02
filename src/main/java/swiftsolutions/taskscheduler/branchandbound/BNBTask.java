package swiftsolutions.taskscheduler.branchandbound;

import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Created by Winston on 8/2/2018.
 */
public class BNBTask implements Serializable{
    int[] _parents;
    int[] _children;
    int _procTime;
    int[] _commCost;
    int _id;
    int _numDependency;
    int _bottomLevel;

    public BNBTask(int id, int[] parents, int[] children, int procTime, int[] commCost, int numDependency, int bottomLevel) {
        _parents = parents;
        _children = children;
        _procTime = procTime;
        _commCost = commCost;
        _id = id;
        _numDependency = numDependency;
        _bottomLevel = bottomLevel;
    }

    public BNBTask copy() {
        return new BNBTask(
                _id,
                _parents,
                _children,
                _procTime,
                _commCost,
                _numDependency,
                _bottomLevel
        );
    }

    public BNBTask(Task task) {
        _id = task.getTaskID();
        _procTime = task.getProcessTime();

        Set<Task> parents = task.getParentTasks();
        Set<Task> children = task.getChildTasks();
        int highestParentId = 0;
        _parents = new int[parents.size()];
        _children = new int[children.size()];

        int i = 0;
        for (Task parent : parents) {
            _parents[i] = parent.getTaskID();
            if (_parents[i] > highestParentId) {
                highestParentId = _parents[i];
            }
            i++;
            _numDependency++;
        }

        i = 0;
        for (Task child : children) {
            _children[i] = child.getTaskID();
            i++;
        }

        if (highestParentId >= 0) {
            _commCost = new int[highestParentId + 1];
            Map<Task, Integer> commCosts = task.getCommunicationCosts();
            commCosts.forEach((Task parent, Integer cost) -> _commCost[parent.getTaskID()] = cost);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        BNBTask other = (BNBTask)obj;
        return _id == other._id;
    }

    @Override
    public int hashCode() {
        return _id;
    }
}
