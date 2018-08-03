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

        Set<Integer> parents = task.getParentTasks();
        Set<Integer> children = task.getChildTasks();
        int highestParentId = -1;
        _parents = new int[parents.size()];
        _children = new int[children.size()];

        int i = 0;
        for (Integer parentID : parents) {
            if (parentID > highestParentId) {
                highestParentId = parentID;
            }
            _parents[i] = parentID;
            i++;
            _numDependency++;
        }

        i = 0;
        for (Integer child : children) {
            _children[i] = child;
            i++;
        }

        if (highestParentId >= 0) {
            _commCost = new int[highestParentId + 1];
            Map<Integer, Integer> commCosts = task.getCommunicationCosts();
            commCosts.forEach((Integer parent, Integer cost) -> _commCost[parent] = cost);
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
