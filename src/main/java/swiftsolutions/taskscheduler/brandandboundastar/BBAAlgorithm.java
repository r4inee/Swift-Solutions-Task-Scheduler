package swiftsolutions.taskscheduler.brandandboundastar;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.util.*;
import java.util.stream.Collectors;

public class BBAAlgorithm implements Algorithm{
	private int _numProcessors;

	private int[][] _tasks; // row represents the task, cols represent { proc time, number of dependencies, bottom level}
	private int[][] _dependencies; // row represents child, col represents parent, value 1 represents is parent 0 if not
	private int[][] _bestFState; // output schedule
	private int[][] _communicationCosts; // row represents the parent, col represents the child, value is the cost



	//	private HashMap<Integer, Integer[]> _taskMap; // map key = task id, map value, array index: 0 = proc time, 1 = dependencies, 2 = bottomlevel
	//	private ArrayList<Pair<Integer,Integer>> _depMap; //arraylist of pairs, pair value B depends on Pair value A
	//	private ArrayList<ArrayList<Pair<Integer, Integer>>> _bestFState; //schedule is now 2d array list of pairs, outter arraylist is processors, inner arraylist is tasks, pair task id and start time
	private Map<Integer, Task> _originalTaskMap;

	public static final int EMPTY = -1;

	// used for schedules in general (including _bestFState)
	public static final int START_TIME = 0;
	public static final int END_TIME = 1;
	public static final int PROCESSOR_INDEX = 2;
	// used for _taskMap
	public static final int PROC_TIME = 0;
	public static final int NUM_DEP = 1;
	public static final int BOTT_LVL = 2;

	public int[][] TestExecute() {
		_tasks = new int[][] {{5,0,16}, {4,1,11}, {7,1,7}};
		_dependencies = new int[][] {{0,0,0}, {1,0,0}, {0,1,0}};
		_communicationCosts = new int[][] {{0,0,0}, {0,0,0}, {0,0,0}};

		int[][] newS = new int[][] {{0,0,-1},{0,0,-1},{0,0,-1}};
		int[][] bestFState = new int[3][3];

		this.BBA(0, -1, -1, -1, 3, 0, new int[] {0,0,0}, _tasks, newS, bestFState, 100);

		return _bestFState;

	}

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
		_originalTaskMap = tasks;
		//find all the leaf nodes
		Set<Task> leafs = tasks.values()
				.stream()
				.filter((Task task) -> task.getChildTasks().size() == 0)
				.collect(Collectors.toSet());
		//Compute the bottom levels for the nodes
		for (Task leaf : leafs) {
			leaf.updateBottomLevel(leaf.getProcessTime());
			getBottomLevels(leaf.getParentTasks(),leaf.getProcessTime());
		}

		//TODO initialize all arrays

		int[][] bestFState = null;
		return null;
	}

	/**
	 * This is the main method that creates the schedules
	 * @param currentTask
	 * @param currentProcessor
	 * @param previousTask
	 * @param previousProcessor
	 * @param numFreeTasks
	 * @param depth
	 * @param B
	 */
	private int BBA(int currentTask, int currentProcessor, int previousTask,
			int previousProcessor, int numFreeTasks, int depth, int[] procEndTimes, int[][] tasks, int[][] s, int[][] bestFState, int B){
		int	initialStateCost = 0;
		int done = 0; //exit flag
		//priority queue for tasks based on cost function
		
		int[] freeTasks = free(s, tasks);
		System.out.println(freeTasks.length + " ARE FREE");
		int idleTime = 0;
		if (freeTasks.length != 0) {
			for (int i = 0; i < numFreeTasks; i++) {
				for (int j = 0; j < _numProcessors; j++) { //add the task to all processors
					depth++;
					int[][] clonedS = new int[s.length][s[START_TIME].length]; //START_TIME used to just to find length
					//copies the schedule instead of re-reference, tasks are removed if branch moves up
					for (int si = 0; si < s.length; si++){
						for (int sj = 0; sj < s[si].length; sj++){
							clonedS[si][sj] = s[si][sj];
						}
					}
					int[] clonedProcEndTimes = Arrays.copyOf(procEndTimes, procEndTimes.length); //copy Processor end times
					//copies the tasks
					int[][] clonedTasks = new int[tasks.length][tasks[PROC_TIME].length];
					for (int ti = 0; ti < tasks.length; ti++){
						for (int tj = 0; tj < tasks.length; tj++){
							clonedTasks[ti][tj] = tasks[ti][tj];
						}
					}
					numFreeTasks = freeTasks.length;
					//add task, not put into separate method to avoid dereference
					int lastEndTime = 0;
					//gets last scheduled task end time, used when adding a new task
					lastEndTime = clonedProcEndTimes[j];
					//gets last scheduled task end time, used when adding a new task
					/*
					 *
					 * TODO:ADD COMMUNICATION TIME CALCULATIONS
					 *
					 */
					//add the task to the schedule
					int taskID = freeTasks[i];
					clonedS[taskID][PROCESSOR_INDEX] = j;
					clonedS[taskID][START_TIME] = lastEndTime;
					clonedS[taskID][END_TIME] = lastEndTime + clonedTasks[taskID][PROC_TIME];
					clonedProcEndTimes[j] = clonedS[taskID][END_TIME];
					System.out.println(taskID + "ID");
						for(int dj = 0; dj < _dependencies.length; dj++) {
							
							if(_dependencies[dj][taskID] == 1)
							{
								clonedTasks[dj][1]--;
								
							}
							
						}
					// TODO IMPLEMENT CHILDREN DEPENDENCIES DECREMENT
					//add task end

					//reset method values
					previousTask = currentTask;
					previousProcessor = currentProcessor;
					currentProcessor = j;
					currentTask = i;
					//calculate data ready time
					int dataReadyTime = dataReadyTime(currentTask, j, clonedS);
					// calculate idle time
					idleTime += dataReadyTime;
					//if cost is lower than B(est) and depth is max, set current best, go back up tree
					if (cost(initialStateCost, clonedS, currentTask, dataReadyTime, idleTime) <= B && depth == _tasks.length){
						System.out.println("one");
						_bestFState = clonedS; // clonedS
						B = cost(initialStateCost,clonedS, currentTask, dataReadyTime, idleTime);
						return 1;
					}
					//if cost is lower than B(est) and depth is max, recursive call
					if (cost(initialStateCost, clonedS, currentTask, dataReadyTime, idleTime) <= B && depth <= _tasks.length){
						System.out.println("two");
						done = BBA(currentTask,currentProcessor,previousTask,previousProcessor,numFreeTasks,depth,clonedProcEndTimes, clonedTasks, clonedS, bestFState,B);
					}
					if (done == 0){
						System.out.println("three");
						depth--;
						idleTime -= dataReadyTime;
					}
					//otherwise if cost is worse than best, return 1 (end recursion)
					return 1;
				}
			}
		}
		return 1;
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
			_originalTaskMap.get(node).updateBottomLevel(currentBottomLevel +
					_originalTaskMap.get(node).getProcessTime());

			if (!_originalTaskMap.get(node).getParentTasks().isEmpty()){
				getBottomLevels(_originalTaskMap.get(node).getParentTasks(),
						currentBottomLevel +
						_originalTaskMap.get(node).getProcessTime());
			}
		}
	}

	/**
	 * Based off the cost function f described where
	 * f = max{Initial(s), idle-time(s), bottom-level(s), DRT(s)}
	 * @param s
	 * @return
	 */
	private int cost(int initialStateCost, int[][] s, int taskID, int dataReadyTime, int idleTime) {
		int cost = 0;
		if (initialStateCost > cost){
			cost = initialStateCost;
		}
		// get the bottom level of the schedule
		int scheduleBottomLevel = s[taskID][START_TIME] + _tasks[taskID][BOTT_LVL];
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

	private Queue<Integer> orderFreeTasks(int[] freeTasks){
		return null;
	}
}
