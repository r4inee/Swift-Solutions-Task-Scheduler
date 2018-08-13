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
	private int[] _procEndTimes; // row represents the processor, col represents end time



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


		return null;
	}

    /**
     * Current does not work because implementation of _taskMap has changed
     * @return
     */
	public ArrayList<ArrayList<Pair<Integer, Integer>>> TestExecute() {

//		_taskMap = new HashMap<Integer, Integer[]>();
//		_taskMap.put(5, new Integer[] {1,0,1});
//		_taskMap.put(7, new Integer[] {1,1,1});
//		_taskMap.put(6, new Integer[] {1,1,1});
//
//		_depMap = new  ArrayList<Pair<Integer,Integer>>();
//		_depMap.add(new Pair<Integer,Integer>(5,6));
//		_depMap.add(new Pair<Integer,Integer>(6,7));
//
//
//		 ArrayList<ArrayList<Pair<Integer,Integer>>> s = new ArrayList<ArrayList<Pair<Integer,Integer>>>();
//		 s.add(new ArrayList<Pair<Integer,Integer>>());
//
//		_numProcessors = 1;
//		BBA(0,0,0,0,0,0, s,99);
//
//
//		return _bestFState;
//


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
			int previousProcessor, int numFreeTasks, int depth, int[][] s, int B){
		int done = 0; //exit flag
		//priority queue for tasks based on cost function
		int[] freeTasks = free(s);
		System.out.println(freeTasks.length + "SIZE");
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
					int[] clonedProcEndTimes = Arrays.copyOf(_procEndTimes, _procEndTimes.length); //copy Processor end times
					//copies the tasks
					int[][] clonedTasks = new int[_tasks.length][_tasks[PROC_TIME].length];
					for (int ti = 0; ti < _tasks.length; ti++){
						for (int tj = 0; tj < _tasks.length; tj++){
							clonedTasks[ti][tj] = _tasks[ti][tj];
						}
					}
					numFreeTasks = freeTasks.length;
					//add task, not put into separate method to avoid dereference
					int lastEndTime = 0;
					//gets last scheduled task end time, used when adding a new task
					lastEndTime = clonedProcEndTimes[j];
//					gets last scheduled task end time, used when adding a new task
//					if(tempS.get(j).size() > 0) {
//						lastEndTime = tempS.get(j).get(tempS.get(j).size()-1).getB() + _taskMap.get(tempS.get(j).get(tempS.get(j).size()-1).getA())[0];
//					}
					/*
					 *
					 * TODO:ADD COMMUNICATION TIME CALCULATIONS
					 *
					 */
					//add the task to the schedule
					clonedS[i][PROCESSOR_INDEX] = j;
					clonedS[i][START_TIME] = lastEndTime;
					clonedS[i][END_TIME] = lastEndTime + clonedTasks[i][PROC_TIME];
					clonedProcEndTimes[j] = clonedS[i][END_TIME];
//					tempS.get(j).add(new Pair<Integer,Integer>(i,lastEndTime));


					//add task end


					//reset method values
					previousTask = currentTask;
					previousProcessor = currentProcessor;
					currentProcessor = j;
					currentTask = i;

					//if cost is lower than B(est) and depth is max, set current best, go back up tree
					if (cost(s) <= B && depth == _tasks.length){
						_bestFState = clonedS;
						B = cost(clonedS);
						return 1;
					}

					//if cost is lower than B(est) and depth is max, recursive call
					if (cost(clonedS) <= B && depth <= _tasks.length){
						done = BBA(currentTask,currentProcessor,previousTask,previousProcessor,numFreeTasks,depth,clonedS,B);
					}

					if (done == 0){
						depth--;
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
     * TODO
     * @param s
     * @return
     */
	private int cost(int[][] s) {
	    int lowerBound = 0;
	    // get the maximum of task start time + bottom level from each node in the module
        for (int i = 0; i < s.size(); i++){
            //Check if tasks tart time is not -1
            if (s.get(i).get(s.get(i).size()-1).getB() != -1){
                //if start time + bottom level is greater than lower bound then make it the new lower bound
                if (s.get(i).get(s.get(i).size()-1).getB() +
                        _taskMap.get(s.get(i).get(s.get(i).size()-1).getA())[BOTT_LVL] > lowerBound){
                    lowerBound = s.get(i).get(s.get(i).size()-1).getB() +
                            _taskMap.get(s.get(i).get(s.get(i).size()-1).getA())[BOTT_LVL];
                }
            }
        }
        return lowerBound;
	}

	/**
	 * This method takes in a schedule and returns an array of free tasks
	 * which are tasks that are not already scheduled and there parents
	 * have all been scheduled
	 * @param s
	 * @return
	 */
	private int[] free(int[][]s) {

		Set<Integer> taskSet = new HashSet<>();
		for (int i = 0; i < _tasks.length; i++){
			taskSet.add(i);
		}
		//loop through all tasks on all processors
		for (int i = 0; i < s.length; i++){
			// if it is scheduled on a processor remove it from the set
			if (s[i][PROCESSOR_INDEX] != EMPTY){
				taskSet.remove(i);
			}
			// if not all parents have been scheduled then remove it form the set
			if (s[i][NUM_DEP] != 0){
				taskSet.remove(i);
			}
		}
		int[] freeTasks = taskSet.stream().mapToInt(Number::intValue).toArray();
		return freeTasks;
//		Map<Integer, Integer[]> _tempMap = (Map<Integer, Integer[]>) _taskMap.clone();
//
//		//loops through all tasks on all processors
//		for(ArrayList<Pair<Integer,Integer>> p: s) {
//
//			for(Pair<Integer,Integer> t: p) {
//
//				//if a task is scheduled on a proc, remove it from free list
//				if(_taskMap.containsKey(t.getA())){
//					_tempMap.remove(t.getA());
//
//					//for all tasks on schedules, decrement dependency count for children
//					for(Pair<Integer, Integer> deps : _depMap) {
//
//						if(deps.getA() == t.getA()) {
//
//							_tempMap.get(deps.getB())[1]--;
//
//						}
//
//					}
//				}
//
//			}
//		}
//
//		//remove all tasks in map which have more than one dependency count
//		_tempMap.entrySet().removeIf(e-> e.getValue()[1] > 0 );
//
//		return _tempMap;
	}
	
	

	private Queue<Integer> orderFreeTasks(int[] freeTasks){
		return null;
	}

	private void listHeuristic(){

	}
}
