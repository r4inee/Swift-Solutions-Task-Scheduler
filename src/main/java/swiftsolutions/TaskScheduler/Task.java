package swiftsolutions.TaskScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represent a single task which needs to be scheduled and is parsed from nodes of an input graph.
 */
public class Task {

    private long _processTime;
    private int _ingoingEdges;
    private Map<Task,Integer> childTaskMap = new HashMap<>(); //Used to map a child and the edge weight to the child

    /*
    Constructor for Task that takes in the process time for the task
     */
    public Task(long processTime){
        this._processTime = processTime;
    }

    
}
