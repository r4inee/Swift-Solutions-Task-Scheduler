package swiftsolutions.TaskScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represent a processor object which contains the current schedule allocated by the scheduler.
 */
public class Processor {
    // Represent the current list of tasks it is performing.
    private Map<Task, int[]> _taskList;
    // Represent the current finish time of this processor
    private int _endTime;
    
    public Processor(){
    	
    	_taskList = new HashMap<Task, int[]>();
    }
    
    public int getEndTime(){
		
    	
    	return _endTime;
    }
    
    public Map<Task, int[]> getTaskList(){
		
    	
    	return _taskList;
    }
    
    public void addTask(Task task, int offset){
    	
    	int[] startAndLengthTimes = new int[2];
    	startAndLengthTimes[0] = _endTime + offset;
    	startAndLengthTimes[1] = startAndLengthTimes + task.processTime; // TODO: 
    	_taskList.put(task, startAndLengthTimes);
    }
}
