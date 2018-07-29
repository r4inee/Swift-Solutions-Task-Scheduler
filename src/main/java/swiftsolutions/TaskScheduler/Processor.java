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
    private Map<Task, int[]> taskList;
    // Represent the current finish time of this processor
    private int endTime;
    
    public Processor(){
    	
    	taskList = new HashMap<Task, int[]>();
    }
    
    public int getEndTime(){
		
    	
    	return endTime;
    }
    
    public Map<Task, int[]> getTaskList(){
		
    	
    	return taskList;
    }
    
    public void addTask(Task task, int offset){
    	
    	int[] startAndLengthTimes = new int[2];
    	startAndLengthTimes[0] = endTime + offset;
    	startAndLengthTimes[1] = startAndLengthTimes + task.length; // TODO: 
    	taskList.put(task, startAndLengthTimes);
    }
}
