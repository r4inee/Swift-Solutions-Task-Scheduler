package swiftsolutions.taskscheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represent a processor object which contains the current schedule allocated by the scheduler.
 */
public class Processor {
    // Represent the current list of tasks it is performing.
    private Map<Task, Long[]> _taskList;
    // Represent the current finish time of this processor
    private Long _endTime;
    
    public Processor(){
    	
    	_taskList = new HashMap<Task, Long[]>();
    }
    
    public Long getEndTime(){
    	
    	return _endTime;
    }
    
    public Map<Task, Long[]> getTaskList(){

		
    	
    	return _taskList;
    }
    
    public void addTask(Task task, int offset){	
    	Long[] startAndLengthTimes = new Long[2];
    	startAndLengthTimes[0] = _endTime + offset;
    	startAndLengthTimes[1] = startAndLengthTimes[1] + task.getProcessTime(); // TODO: 
    	_taskList.put(task, startAndLengthTimes);
    }
}
