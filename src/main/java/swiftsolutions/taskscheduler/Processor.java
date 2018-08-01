package swiftsolutions.taskscheduler;

import swiftsolutions.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;



/**
 * This class represent a processor object which contains the current schedule allocated by the scheduler.
 */
public class Processor implements Serializable{
    // Represent the current list of tasks it is performing.
    private Map<Task, Pair<Long, Long>> _taskList;
    // Represent the current finish time of this processor
    private Long _endTime;
    
    public Processor(){
    	_taskList = new HashMap<Task, Pair<Long, Long>>();
        _endTime = new Long(0);
    }
    
    public Long getEndTime(){
    	return _endTime;
    }
    
    public Map<Task, Pair<Long, Long>> getTaskList(){
		
    	
    	return _taskList;
    }
    
    public void addTask(Task task, int offset){
    	long taskStart = _endTime + offset;
    	_endTime = taskStart + task.getProcessTime();
    	_taskList.put(task, new Pair<Long, Long>(taskStart, _endTime));
    }

    public long getIdleTime() {
        long endTime = _endTime;
        for (Task task : _taskList.keySet()) {
            endTime -= task.getProcessTime();
        }
        return endTime;
    }

}
