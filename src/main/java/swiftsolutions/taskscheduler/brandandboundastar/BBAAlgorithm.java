package swiftsolutions.taskscheduler.brandandboundastar;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class BBAAlgorithm implements Algorithm{
	private int _numProcessors;

	private int[][] _tasks; // row represents the task, cols represent { proc time, number of dependencies, bottom level}
	private int[][] _dependencies; // row represents child, col represents parent, value 1 represents is parent 0 if not
	private int[][] _bestFState; // output schedule
	private int[][] _communicationCosts; // row represents the parent, col represents the child, value is the cost
	private Map<Integer, Task> _taskMap;

	public static final int EMPTY = -1;
	public static final	int SCHEDULE_COL_SIZE = 3;
	// used for schedules in general (including _bestFState)
	public static final int START_TIME = 0;
	public static final int END_TIME = 1;
	public static final int PROCESSOR_INDEX = 2;
	// used for _tasks
	public static final int PROC_TIME = 0;
	public static final int NUM_DEP = 1;
	public static final int BOTTOM_LVL = 2;

	/**
	 * Overrides Algorithm setProcessors
	 * See Algorithm#setProcessors()
	 * @param processors
	 */
	@Override
	public void setProcessors(int processors) {
		_numProcessors = processors;
	}

	/**
	 * Overrides Algorithm execute
	 * See Algorithm#execute()
	 * @param tasks tasks that will be scheduled
	 * @return
	 */
	@Override
	public Schedule execute(Map<Integer, Task> tasks) {
		_taskMap = tasks;
		Set<Task> leafs = tasks.values() //find all the leaf nodes
				.stream()
				.filter((Task task) -> task.getChildTasks().size() == 0)
				.collect(Collectors.toSet());
		for (Task leaf : leafs) { //Compute the bottom levels for the nodes
			leaf.updateBottomLevel(leaf.getProcessTime());
			getBottomLevels(leaf.getParentTasks(),leaf.getProcessTime());
		}
		convertTasks(); //converts the tasks into the 2D array format
		int[] procEndTimes = new int[_numProcessors]; // create a 2D array with row size number of processors, 1 col
		int[][] initialSchedule = new int[_tasks.length][SCHEDULE_COL_SIZE];
		// need to make the processor value on initial schedule -1;
		for (int i = 0; i < initialSchedule.length; i++){
			initialSchedule[i][PROCESSOR_INDEX] = EMPTY;
		}
		int B = 0; // initialize B as the max bottom level of all root tasks
		for (Integer taskID : _taskMap.keySet()){
			if (_taskMap.get(taskID).getNumDependency() == 0){
				if (_taskMap.get(taskID).getBottomLevel() > B){
					B = _taskMap.get(taskID).getBottomLevel();
				}
			}
		}
		BBA(0,-1,-1,-1,
				_tasks.length, 0, procEndTimes, _tasks, initialSchedule, B); //Call the recursion algorithm
		return convertSchedule(_bestFState);
	}

	/**
	 * This is the main method that creates the schedules implemented using the pseudo code. Hybrid Branch and Bound
	 * with A* algorithm
	 * @param currentTask
	 * @param currentProcessor
	 * @param previousTask
	 * @param previousProcessor
	 * @param numFreeTasks
	 * @param depth
	 * @param B
	 */
	private int BBA(int currentTask, int currentProcessor, int previousTask,
			int previousProcessor, int numFreeTasks, int depth, int[] procEndTimes, int[][] tasks, int[][] s, int B){
		int done = 0; //exit flag
		//priority queue for tasks based on cost function
		int[] freeTasks = free(s, tasks);
		int idleTime = 0;
		if (freeTasks.length != 0) {
			for (int i = 0; i < numFreeTasks; i++) {
				for (int j = 0; j < _numProcessors; j++) { //add the task to all processors
					depth++;
					int[][] clonedS = new int[s.length][s[START_TIME].length]; //START_TIME used to just to find length
					for (int si = 0; si < s.length; si++){ //copies the schedule instead of re-reference, tasks are removed if branch moves up
						for (int sj = 0; sj < s[si].length; sj++){
							clonedS[si][sj] = s[si][sj];
						}
					}
					int[] clonedProcEndTimes = Arrays.copyOf(procEndTimes, procEndTimes.length); //copy Processor end times
					int[][] clonedTasks = new int[tasks.length][tasks[PROC_TIME].length]; //copies the tasks
					for (int ti = 0; ti < tasks.length; ti++){
						for (int tj = 0; tj < tasks.length; tj++){
							clonedTasks[ti][tj] = tasks[ti][tj];
						}
					}
					numFreeTasks = freeTasks.length;
					int lastEndTime = clonedProcEndTimes[j]; //gets last scheduled task end time, used when adding a new task
					int taskID = freeTasks[i]; //select task to add
					clonedS[taskID][PROCESSOR_INDEX] = j;
					clonedS[taskID][START_TIME] = lastEndTime;
					clonedS[taskID][END_TIME] = lastEndTime + clonedTasks[taskID][PROC_TIME];
					clonedProcEndTimes[j] = clonedS[taskID][END_TIME];
					System.out.println(taskID + "ID");
					for(int dj = 0; dj < _dependencies.length; dj++) {
						if(_dependencies[dj][taskID] == 1){
							clonedTasks[dj][1]--;
						}
					}
					previousTask = currentTask; //reset method values
					previousProcessor = currentProcessor;
					currentProcessor = j;
					currentTask = taskID;
					int dataReadyTime = dataReadyTime(currentTask, j, clonedS); //calculate data ready time
					idleTime += dataReadyTime; // calculate idle time
					//if cost is lower than B(est) and depth is max, set current best, go back up tree
					if (cost(clonedS, currentTask, dataReadyTime, idleTime) <= B && depth == _tasks.length){
						_bestFState = clonedS; // clonedS
						B = cost(clonedS, currentTask, dataReadyTime, idleTime);
						return 1;
					}
					//if cost is lower than B(est) and depth is max, recursive call
					if (cost(clonedS, currentTask, dataReadyTime, idleTime) <= B && depth <= _tasks.length){
						done = BBA(currentTask,currentProcessor,previousTask,
								previousProcessor,numFreeTasks,depth,clonedProcEndTimes, clonedTasks, clonedS,B);
					}
					if (done == 0){
						depth--;
						idleTime -= dataReadyTime;
					}
					//otherwise if cost is worse than best, return 1 (end recursion)
					return 1;
				}
			}
		}
		return 1; // should not get here
	}

	/**
	 * Recursive function that updates the bottom levels
	 * of the nodes in the input set and all
	 * the nodes of that parent
	 * @param nodes
	 * @param currentBottomLevel
	 */
	private void getBottomLevels(Set<Integer> nodes, int currentBottomLevel){
		for (Integer node : nodes){
			_taskMap.get(node).updateBottomLevel(currentBottomLevel +
					_taskMap.get(node).getProcessTime());

			if (!_taskMap.get(node).getParentTasks().isEmpty()){
				getBottomLevels(_taskMap.get(node).getParentTasks(),
						currentBottomLevel +
						_taskMap.get(node).getProcessTime());
			}
		}
	}

	/**
	 * Based off the cost function f described where
	 * f = max{Initial(s), idle-time(s), bottom-level(s), DRT(s)}
	 * @param s
	 * @return
	 */
	private int cost(int[][] s, int taskID, int dataReadyTime, int idleTime) {
		//cost of the initial state
		int cost = 0;
		// get the bottom level of the schedule
		int scheduleBottomLevel = s[taskID][START_TIME] + _tasks[taskID][BOTTOM_LVL];
		if (scheduleBottomLevel > idleTime){
			cost = scheduleBottomLevel;
		} else {
			cost = idleTime;
		}
		if (dataReadyTime > cost){
			cost = dataReadyTime;
		}
		return cost;
	}

	/**
	 * Method to find the data ready time for a task that is
	 * to be scheduled on a processor
	 * @param taskID
	 * @param processor
	 * @param s
	 * @return
	 */
	private int dataReadyTime(int taskID, int processor, int[][] s){
		int parentID = 0;
		int dataReadyTime = 0;
		// find the latest end time of a parent and it's ID
		for (int i = 0; i < _dependencies[taskID].length; i++){
			if (_dependencies[taskID][i] == 1){
				if (s[_dependencies[taskID][i]][END_TIME] > dataReadyTime){
					dataReadyTime = s[_dependencies[taskID][i]][END_TIME];
					parentID = _dependencies[taskID][i];
				}
			}
		}
		// if the parent is the on the same processor as the task to be scheduled there is no communication cost
		if (s[parentID][PROCESSOR_INDEX] == processor){
			return dataReadyTime;
		} else { // add the communication cost of the parent to the task to be scheduled
			return dataReadyTime + _communicationCosts[parentID][taskID];
		}
	}

	/**
	 * This method takes in a schedule and returns an array of free tasks
	 * which are tasks that are not already scheduled and there parents
	 * have all been scheduled
	 * @param s
	 * @return
	 */
	private int[] free(int[][]s, int[][] tasks) {
		Set<Integer> taskSet = new HashSet<>();
		for (int i = 0; i < tasks.length; i++){
			taskSet.add(i);
		}
		//loop through all tasks on all processors
		for (int i = 0; i < s.length; i++){
			// if it is scheduled on a processor remove it from the set
			if (s[i][PROCESSOR_INDEX] != EMPTY){
				taskSet.remove(i);
			}
			// if not all parents have been scheduled then remove it form the set
			if (tasks[i][NUM_DEP] != 0){
				taskSet.remove(i);
			}
		}

		int[] freeTasks = taskSet.stream().mapToInt(Number::intValue).toArray();
		return freeTasks;
	}

	/**
	 * This method is used to convertTasks the task map passed into
	 * execute() into the arrays used to map the data in this
	 * class
	 */
	private void convertTasks(){
		//Parse the Task into the 2D array tasks
		for (int taskID: _taskMap.keySet()){
			_tasks[taskID][PROC_TIME] = _taskMap.get(taskID).getProcessTime();
			_tasks[taskID][NUM_DEP] = _taskMap.get(taskID).getNumDependency();
			_tasks[taskID][BOTTOM_LVL] = _taskMap.get(taskID).getBottomLevel();
		}
		//Parse the parents into the 2D array dependencies
		for (int taskID: _taskMap.keySet()){
			for (int parentID: _taskMap.get(taskID).getParentTasks()){
				_dependencies[taskID][parentID] = 1;
				_communicationCosts[parentID][taskID] = _taskMap.get(taskID).getCommunicationCosts(parentID);
			}
		}

	}

	/**
	 * Convert the final 2D array schedule back into a schedule that is to be
	 * returned be the execute method
	 * @param schedule
	 * @return
	 */
	private Schedule convertSchedule(int[][] schedule){
		Map<Integer,Pair<Integer,Integer>> scheduleMap = new LinkedHashMap<>();
		for (int i = 0; i < schedule.length; i++){
			int startTime = schedule[i][START_TIME];
			int processor = schedule[i][PROCESSOR_INDEX];
			scheduleMap.put(i,new Pair<>(processor,startTime));
		}
		return new Schedule(scheduleMap,_numProcessors);
	}
}
