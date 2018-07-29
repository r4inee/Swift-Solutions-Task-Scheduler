package swiftsolutions.taskscheduler;

import java.util.List;

/**
 * This abstract class contains information which is used by every type of schedule
 */
public class Schedule {
    
	private Schedule _parentSchedule ;
	private Schedule _childSchedule ;
	private List<Processor> _processors;
	
	public Schedule(Schedule parentSchedule, Schedule childSchedule){
		
		this._parentSchedule = parentSchedule;
		this._childSchedule = childSchedule;
		
	}
	
	public void addTask(Task task, int processorNumber){
		
		Processor processor = this._processors.get(processorNumber);
		processor.addTask(task, 0);
		
	}

	public void addProcessor(Processor processor){
		
		this._processors.add(processor);
	}
}
