package swiftsolutions.taskscheduler;

import java.io.Serializable;
import java.util.*;

/**
 * This class represent a single task which needs to be scheduled and is parsed from nodes of an input graph.
 */
public class Task implements Serializable{
    private int _taskID;
    private int _processTime;
    private Map<Integer, Integer> _communicationCosts;
    private Set<Integer> _parentTasks;
    private Set<Integer> _childTasks; // Refers to map communication cost with its children
    private int _bottomLevel;

    /**
     * Constructor which takes an unique ID identifying this task and the processing time of the task as input.
     * @param processTime
     */
    public Task(int taskID, int processTime){
        _taskID = taskID;
        _processTime = processTime;
        _communicationCosts = new HashMap<>();
        _parentTasks = new HashSet<>();
        _childTasks = new HashSet<>();
    }

    public Task createCopy() {
        Task task = new Task(_taskID, _processTime);
        _communicationCosts = task._communicationCosts;
        _parentTasks = task._parentTasks;
        return task;
    }

    /**
     * Method to add a child task to the task
     * @param task The child task to be added
     */
    public void addChild(Integer task){
        _childTasks.add(task);
    }

    /**
     * Method for initialising dependency with a communication cost in the child instance.
     */
    public void addParent(Integer task, int communicationCost) {
        _communicationCosts.put(task, communicationCost);
        _parentTasks.add(task);
    }

    /**
     * Getter for obtaining the communication cost with its parent.
     * @param parentID
     * @return The cost of communication, defaults to 0.
     */
    public int getCommunicationCosts(Integer parentID) {
        if (_communicationCosts.keySet().contains(parentID)) {
            return _communicationCosts.get(parentID);
        }
        return 0;
    }

    /**
     * Getter for the process time
     * @return
     */
    public int getProcessTime(){
        return this._processTime;
    }

    /**
     * Getter for getting the unique task ID.
     * @return
     */
    public int getTaskID() {
        return this._taskID;
    }

    /**
     * Getter for getting the number of dependencies.
     * @return
     */
    public int getNumDependency(){
        return _parentTasks.size();
    }

    public Set<Integer> getParentTasks() {
        return _parentTasks;
    }

    public Set<Integer> getChildTasks() { return _childTasks; }

    public Map<Integer, Integer> getCommunicationCosts() { return _communicationCosts; }

    public void offsetId(int offset) {
        _taskID -= offset;
    }

    public void updateBottomLevel(int bottomLevel) {
        if (bottomLevel > _bottomLevel) {
            _bottomLevel = bottomLevel;
        }
    }

    public int getBottomLevel() {
        return _bottomLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        return ((Task)obj)._taskID == _taskID;
    }

    @Override
    public int hashCode() {
        return _taskID;
    }
}
