package swiftsolutions.taskscheduler;

import swiftsolutions.util.Pair;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains information which represents an optimal schedule outputted by an algorithm.
 */
public class Schedule {

	public static final int START_TIME = 0;
	public static final int END_TIME = 1;
	public static final int PROCCESSOR_INDEX = 2;
	public static final int EMPTY = -1;


	// Map for Task to Pair<ProcessorID, StartTime>
	private Map<Integer, Pair<Integer, Integer>> _taskToProcessorMap;
	private int _numProc;

	public Schedule(Map<Integer, Pair<Integer, Integer>> taskToProcessorMap, int numProc) {
		_taskToProcessorMap = taskToProcessorMap;
		_numProc = numProc;
	}

	/**
	 * Normalizes the taskIDS.
	 * @param taskMap the map of tasks, keyed by the task id and has a corresponding value of the information of that
	 *                task.
	 */
	public void convertTaskID(Map<Integer, Task> taskMap) {
		Map<Integer, Pair<Integer, Integer>> map = new HashMap<>();
		for (Integer offsetID : _taskToProcessorMap.keySet()) {
			Integer id = taskMap.get(offsetID).getTaskID();
			map.put(id, _taskToProcessorMap.get(offsetID));
		}
		_taskToProcessorMap = map;
	}

	/**
	 * Splits the tasks by processors
	 * @return a map of processor id, to list of tasks that have been scheduled on that processor.
	 */
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

	/**
	 * @return the Schedule as a string to be displayed on the command line.
	 */
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

	/**
	 * Returns the processor that the input task as been scheduled on.
	 * @param task that we want to get the processor of that it has been scheduled on.
	 * @return the processor of the task input.
	 */
	public Pair<Integer, Integer> getProcessor(Integer task) {
		return _taskToProcessorMap.get(task);
	}

	/**
	 * @return the total number of processors that we have been scheduling tasks on.
	 */
	public int getNumProc() {
		return _numProc;
	}

	/**
	 * @return a map of processor id, to list of tasks that have been scheduled on that processor.
	 */
	public Map<Integer, Pair<Integer, Integer>> getTaskToProcessorMap() {
		return _taskToProcessorMap;
	}
}
