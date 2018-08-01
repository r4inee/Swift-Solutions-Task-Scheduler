package swiftsolutions.taskscheduler;

import swiftsolutions.util.Pair;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This abstract class contains information which is used by every type of schedule
 */
public class Schedule implements Serializable{

	private Processor[] _processors;
	private boolean _initialTask;
	private int _activeProcessor;
	
	public Schedule(int numProcessors){
		_processors = new Processor[numProcessors];
		for (int i = 0; i < numProcessors; i++) {
			_processors[i] = new Processor();
		}
		_initialTask = true;
	}
	
	public void addTask(Task task, int processorNumber){
		System.out.println("Task: " + task.getTaskID() + " Proc: " + processorNumber);
		long offset = 0;
		if (!_initialTask) {
			for (Task parent : task.getParentTasks()) {
				Pair<Long, Long> info = findProc(parent);
				if (info.a != processorNumber) {
					System.out.println(parent.getCommunicationCosts(task) + "com");
				}
			}
		} else {
			_initialTask = false;
		}
		_activeProcessor = processorNumber;
		System.out.println("Offset: " + offset);
		_processors[processorNumber].addTask(task, (int)offset);
	}

	private Pair<Long, Long> findProc(Task task) {
		for (int i = 0; i < _processors.length; i++) {
			Map<Task, Pair<Long, Long>> taskList = _processors[i].getTaskList();
			if (taskList.containsKey(task)) {
				return new Pair<>((long)i, taskList.get(task).b);
			}
		}
		return new Pair<>(new Long(-1), new Long(-1));
	}

	public long getIdleTime() {
		int sum = 0;
		for (Processor processor : _processors) {
			sum += processor.getIdleTime();
		}
		return sum;
	}

	public long getCost() {
		long max = 0;
		for (Processor processor : _processors) {
			if (processor.getEndTime() > max) {
				max = processor.getEndTime();
			}
		}
		return max;
	}


	public Processor[] getProcessors() {
		return _processors;
	}

}
