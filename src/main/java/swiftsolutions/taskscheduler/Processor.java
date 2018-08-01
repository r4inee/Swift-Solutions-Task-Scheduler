package swiftsolutions.taskscheduler;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.misc.Pair;

/**
 * This class represent a processor object which contains the current schedule allocated by the scheduler.
 */
public class Processor {
    // Represent the current list of tasks it is performing.
    private Map<Task, Pair<Long, Long>> _taskList;
    // Represent the current finish time of this processor
    private Long _endTime;
    
    public Processor(){
    	_taskList = new HashMap<Task, Pair<Long, Long>>();
    }
    
    public Long getEndTime(){
    	return _endTime;
    }
    
    public Map<Task, Pair<Long, Long>> getTaskList(){
		
    	
    	return _taskList;
    }
    
    public void addTask(Task task, int offset){
    	Long taskStart = _endTime + offset;
    	Long taskEnd = taskStart + task.getProcessTime(); 
    	_taskList.put(task, new Pair<Long, Long>(taskStart, taskEnd));
    }
}
