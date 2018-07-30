package swiftsolutions.taskscheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * This class represent a single task which needs to be scheduled and is parsed from nodes of an input graph.
 */
public class Task {
    private long _processTime;
    private int _ingoingEdges; //Default 0
    private Map<Task,Integer> _childTaskMap = new HashMap<>(); //Used to map a child and the edge weight to the child
    private List<Task> _childList = new ArrayList<>(); //Store the children

    /*
    Constructor for Task that takes in the process time for the task
     */
    public Task(long processTime){
        _processTime = processTime;
    }

    /**
     * Method to add a child task to the task
     * @param childTask
     * @param edgeWeight
     */
    public void addChild(Task childTask, Integer edgeWeight){
        childTask.increaseIngoingEdges();
        _childTaskMap.put(childTask,edgeWeight);
        _childList.add(childTask);
    }

    /**
     * Method to get the communication time to the task in the input
     * parameter where the task in the input parameter is a child
     * of this task
     * @param nextTask
     * @return
     */
    public Integer getCommunicationTime(Task nextTask){
        return _childTaskMap.get(nextTask);
    }

    /**
     * Getter for the process time
     * @return
     */
    public long getProcessTime(){
        return _processTime;
    }

    /**
     * Getter for ingoingEdges
     * @return
     */
    public int getIngoingEdges(){
        return _ingoingEdges;
    }

    /**
     * Method that returns the list of all child tasks
     * from this task
     * @return
     */
    public List<Task> getChildList(){
        return _childList;
    }

    /**
     * Increases the count of ingoing edges to the task.
     * This is only incremented when the task is added to
     * a parent task
     */
    private void increaseIngoingEdges(){
        _ingoingEdges++;
    }
}
