package swiftsolutions.taskscheduler;

import swiftsolutions.util.Pair;

import java.io.Serializable;
import java.util.*;

/**
 * This class represent a single task which needs to be scheduled and is parsed from nodes of an input graph.
 */
public class Task implements Serializable{
    private int _taskID;
    private int _processTime;
    private int _numDependency;
    private Map<Task, Integer> _communicationCosts; // Refers to map communication cost with its parents.
    private int _bottomLevel;

    private Set<Task> _childTasks; // Contains all the tasks which has a dependency on the this instance.
    private Set<Task> _parentTasks;

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
        _numDependency = task._numDependency;
        _communicationCosts = task._communicationCosts;
        _childTasks = task._childTasks;
        _parentTasks = task._parentTasks;
        return task;
    }

    /**
     * Method to add a child task to the task
     * @param task The child task to be added
     */
    public void addChild(Task task, int communicationCost){
        this._childTasks.add(task);
        task.addDependency(this, communicationCost);
    }

    public void addParent(Task task) {
        this._parentTasks.add(task);
    }

    /**
     * Method to schedule this task and reduce (relax) dependencies on its children
     */
    public void scheduleTask() {
        for (Task task : this._childTasks) {
            task.removeDependency();
        }
    }

    /**
     * Method for initialising dependency with a communication cost in the parent instance.
     */
    private void addDependency(Task task, int communicationCost) {
        this._communicationCosts.put(task, communicationCost);
        _numDependency++;
    }
    /**
     * Method for decrementing dependency count in the child instance after parent has been scheduled.
     */
    private void removeDependency() {
        _numDependency--;
    }

    /**
     * Getter for obtaining the communication cost with its parent.
     * @param task The parent of the task.
     * @return The cost of communication, defaults to 0.
     */
    public int getCommunicationCosts(Task task) {
        if (_communicationCosts.keySet().contains(task)) {
            return _communicationCosts.get(task);
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
     * Getter for getting the  number of dependencies.
     * @return
     */
    public int getNumDependency(){
        return this._numDependency;
    }

    public Set<Task> getParentTasks() {
        return _parentTasks;
    }

    public Set<Task> getChildTasks() { return _childTasks; }

    public Map<Task, Integer> getCommunicationCosts() { return _communicationCosts; }

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
