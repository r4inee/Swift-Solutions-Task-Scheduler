package swiftsolutions.taskscheduler.brandandboundastar;

import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;

public class BBAAlgorithm implements Algorithm{

    private int _numProcessors;
    private int[][] _taskMap; // rows = task, col = {cost, bottom-level}
    private int[][] _parentMap; // row = task, col = parents, value = 1 or 0
    private int[][] _comCostMap; // row = task, col = parent, value = communication cost

    public static final int POS_PROC = 0;



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

        return null;
    }

    /**
     * BBA algorithm
     * B is the overestimate of the schedule length
     * bestFState is lower f-value of the best schedule
     * @param currentTask
     * @param currentProcessor
     * @param previousTask
     * @param previousProcessor
     * @param numFreeTasks
     * @param depth
     * @param bestFState
     * @param B
     */
    private void BBA(int currentTask, int currentProcessor, int previousTask,
                     int previousProcessor, int numFreeTasks, int depth, int[][] s,
                     int bestFState, int B){
        int done = 0; //exit flag
        int[] free = free(s);
        if (free.length != 0) {
            for (int i = 0; i < numFreeTasks; i++) {
                for (int j = 0; j < _numProcessors; j++) {
                    depth++;
                    sanitizeSchedule();
                    numFreeTasks = free.length;

                }
            }
        }

    }

    private void addTask(int[][] s, int task, int processor){
        int processorEndTime;
        s[task][POS_PROC] =  processor;
    }

    private int[] free(int[][] s){
        return null ;
    }

    private void sanitizeSchedule(){

    }
    private void listHeuristic(){

    }
}
