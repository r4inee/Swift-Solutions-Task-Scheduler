package swiftsolutions.taskscheduler;

import java.util.*;

/**
 * This class represent a single task which needs to be scheduled and is parsed from nodes of an input graph.
 */
public class Task {
    private int taskID;
    private int processTime;
    private int numDependency;
    private Map<Task, Integer> communicationCosts; // Refers to map communication cost with its parents.

    private Set<Task> childTasks = new HashSet<>(); // Contains all the tasks which has a dependency on the this instance.

    /**
     * Constructor which takes an unique ID identifying this task and the processing time of the task as input.
     * @param processTime
     */
    public Task(int taskID, int processTime){
        this.taskID = taskID;
        this.processTime = processTime;
        this.communicationCosts = new HashMap<>();
    }

    /**
     * Method to add a child task to the task
     * @param task The child task to be added
     */
    public void addChild(Task task, int communicationCost){
        this.childTasks.add(task);
        task.addDependency(this, communicationCost);
    }

    /**
     * Method to schedule this task and reduce (relax) dependencies on its children
     */
    public void scheduleTask() {
        for (Task task : this.childTasks) {
            task.removeDependency();
        }
    }

    /**
     * Method for initialising dependency with a communication cost in the child instance.
     */
    private void addDependency(Task task, int communicationCost) {
        this.communicationCosts.put(task, communicationCost);
        numDependency++;
    }
    /**
     * Method for decrementing dependency count in the child instance after parent has been scheduled.
     */
    private void removeDependency() {
        numDependency--;
    }

    /**
     * Getter for obtaining the communication cost with its parent.
     * @param task The parent of the task.
     * @return The cost of communication, defaults to 0.
     */
    public int getCommunicationCosts(Task task) {
        if (communicationCosts.keySet().contains(task)) {
            return communicationCosts.get(task);
        }
        return 0;
    }

    /**
     * Getter for the process time
     * @return
     */
    public int getProcessTime(){
        return this.processTime;
    }

    /**
     * Getter for getting the unique task ID.
     * @return
     */
    public int getTaskID() {
        return this.taskID;
    }

    /**
     * Getter for getting the  number of dependencies.
     * @return
     */
    public int getNumDependency(){
        return this.numDependency;
    }
}
