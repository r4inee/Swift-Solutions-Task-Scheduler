package swiftsolutions.taskscheduler;

import swiftsolutions.util.Pair;

import java.io.Serializable;
import java.util.*;

/**
 * This class contains information which represents an optimal schedule outputted by an algorithm.
 */
public class Schedule {

	private Map<Integer, Pair<Integer, Integer>> _taskToProcessorMap;
	private int _numProc;

	public Schedule(Map<Integer, Pair<Integer, Integer>> taskToProcessorMap, int numProc) {
		_taskToProcessorMap = taskToProcessorMap;
		_numProc = numProc;
	}

	public Pair<Integer, Integer> getProcessor(Task task) {
		return _taskToProcessorMap.get(task.getTaskID());
	}

	public int getNumProc() {
		return _numProc;
	}

	public Map<Integer, ArrayList<Integer>> splitByProcessor() {
		Map<Integer, ArrayList<Integer>> procMap = new LinkedHashMap<>();
		for (Integer taskID : _taskToProcessorMap.keySet()) {
			Integer procID = _taskToProcessorMap.get(taskID).getA();
			procMap.get(procID).add(taskID);
		}
		return procMap;
	}

	public String getOutputString() {
		String output = "";
		Map<Integer, ArrayList<Integer>> procMap = splitByProcessor();
		for (Integer procID : procMap.keySet()) {
			output += "==========" + procID + "==========";
			for (Integer taskID: procMap.get(procID)) {
				Pair<Integer, Integer> info = _taskToProcessorMap.get(taskID);
				output += "ID: " + taskID + " Start: " + info.getB() + " Processor: " + info.getA() + "\n";
			}
		}
		return output;
	}
}
