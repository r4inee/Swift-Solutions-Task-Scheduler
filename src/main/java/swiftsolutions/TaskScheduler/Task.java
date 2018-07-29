package swiftsolutions.TaskScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent a single task which needs to be scheduled and is parsed from nodes of an input graph.
 */
public class Task {

    private long _processTime;
    private int _ingoingEdges;
    private List<Task> _childList = new ArrayList<>();


    public Task(long processTime){
        this._processTime = processTime;
    }

    public List<Task> getChildList(){
        return this._childList;
    }
}
