package swiftsolutions.taskscheduler;

import javafx.beans.binding.IntegerBinding;
import swiftsolutions.util.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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

	public void convertTaskID(Map<Integer, Task> taskMap) {
		Map<Integer, Pair<Integer, Integer>> map = new HashMap<>();
		for (Integer offsetID : _taskToProcessorMap.keySet()) {
			Integer id = taskMap.get(offsetID).getTaskID();
			map.put(id, _taskToProcessorMap.get(offsetID));
		}
		_taskToProcessorMap = map;
	}

	private Map<Integer, ArrayList<Integer>> splitByProcessor() {
		Map<Integer, ArrayList<Integer>> procMap = new HashMap<>();
		for (Integer taskID : _taskToProcessorMap.keySet()) {
			Integer procID = _taskToProcessorMap.get(taskID).getA();
			if (!procMap.containsKey(procID)) {
				procMap.put(procID, new ArrayList<>());
			}
			procMap.get(procID).add(taskID);
		}
		return procMap;
	}

	public String getOutputString() {
		String output = "";
		Map<Integer, ArrayList<Integer>> procMap = splitByProcessor();
		List<Integer> processors = procMap.keySet().stream().collect(Collectors.toList());
		Collections.sort(processors);
		for (Integer procID : procMap.keySet()) {
			output += "==========" + procID + "==========\n";

			List<Pair<Integer, Integer>> taskStartTime = procMap.get(procID)
					.stream()
					.map((Integer task) -> {
						int startTime = _taskToProcessorMap.get(task).getB();
						return new Pair<Integer, Integer>(task, startTime);
					}).collect(Collectors.toList());
			taskStartTime.sort(Comparator.comparing(Pair::getB));
			for (Pair<Integer, Integer> info: taskStartTime) {
				output += "ID: " + info.getA() + " Start: " + info.getB() + " Processor: " + procID + "\n";
			}
		}
		return output;
	}

	public Pair<Integer, Integer> getProcessor(Integer task) {
		return _taskToProcessorMap.get(task);
	}
	public int getNumProc() {
		return _numProc;
	}
}
