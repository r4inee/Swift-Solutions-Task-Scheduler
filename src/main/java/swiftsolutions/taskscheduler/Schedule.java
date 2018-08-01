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

	public Schedule(int numProcessors){
		_processors = new Processor[numProcessors];
		for (int i = 0; i < numProcessors; i++) {
			_processors[i] = new Processor();
		}
	}
	
	public void addTask(Task task, int processorNumber){
		long offset = 0;
		for (Task parent : task.getParentTasks()) {
			Pair<Long, Long> info = findProc(parent);
			long tmpOffset = info.b;
			if (processorNumber != info.a) {
				tmpOffset += task.getCommunicationCosts(parent);
			}
			if (tmpOffset > offset) {
				offset = tmpOffset;
			}
		}
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

	public String getOutputString() {
		String output = "";
		for (int i = 0; i < _processors.length; i++) {
			output += "-------" + i + "-------\n";
			Map<Task, Pair<Long, Long>> taskList = _processors[i].getTaskList();
			Set<Task> tasks = taskList.keySet();
			for (Task task : tasks) {
				Pair<Long, Long> info = taskList.get(task);
				output += "ID: " + task.getTaskID() + " ProcTime: " + task.getProcessTime()
						+ " Start: " + info.a + " End: " + info.b + "\n";
			}
		}

		return output;
	}

}
