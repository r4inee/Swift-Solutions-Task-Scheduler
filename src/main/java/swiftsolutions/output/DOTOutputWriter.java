package swiftsolutions.output;

import swiftsolutions.exceptions.OutputException;
import swiftsolutions.interfaces.output.OutputWriter;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of OutputWriter for the scheduling application. See OutputWriter.
 */
public class DOTOutputWriter implements OutputWriter {

    private BufferedWriter _writer;
    private Map<Integer, Task> _taskMap;
    private Map<Integer, Task> _offsetTaskMap;

    /**
     * The function that will write the output.
     * @param filename the file for the output to be written into.
     * @param schedule the schedule that will be written into the output file.
     * @param offsetTaskMap the task information from the input.
     * @throws OutputException if the task information or schedule was malformed.
     */
    @Override
    public void serialize(String filename, Schedule schedule, Map<Integer, Task> offsetTaskMap) throws OutputException {
        _offsetTaskMap = offsetTaskMap;
        _taskMap = convertTaskID(offsetTaskMap);
        String outputFile = new File(filename).getName();

        String outputFileName = new File(filename).getName();

        // Verify that the output file is valid (not "")
        if (outputFile.length() > 1) {
            outputFile = outputFile.substring(0, 1).toUpperCase() + outputFile.substring(1);
        } else {
            outputFile = outputFile.toUpperCase();
        }

        // Take away the file extensive if specified.
        if(outputFile.lastIndexOf(".") != -1 && outputFile.lastIndexOf(".") != 0) {
            outputFile = outputFile.substring(0, outputFile.lastIndexOf("."));
            outputFileName = outputFileName.substring(0, outputFileName.lastIndexOf("."));
        }

        // Write the output file.
        try {
            _writer = new BufferedWriter(new FileWriter(outputFileName + "-output.dot"));

            _writer.write("digraph \"output" + outputFile + "\" {\n");

            for (Integer task :_taskMap.keySet()) {
                Pair<Integer, Integer> proc = schedule.getProcessor(task);
                if (proc == null) {
                    continue;
                }
                writeNode(task, proc.getA(), proc.getB());
            }
            for (Integer child : _taskMap.keySet()) {
                for (Integer parent : _taskMap.get(child).getParentTasks()) {
                    writeEdge(parent, child, _taskMap.get(child).getCommunicationCosts(parent));
                }

            }
            _writer.write("}");
            _writer.close();
        } catch (IOException e) {
            throw new OutputException("Error occur when writing output to file");
        }

    }

    /**
     * Used to write a node to the output file.
     * @param task the task to be written (node).
     * @param processorID the processorId that the task has been scheduled on.
     * @param startTime the start time of the node.
     * @throws IOException if the node, or arguments are malformed.
     */
    private void writeNode(Integer task, int processorID, int startTime) throws IOException {
        int offsetProcID = processorID+1;
        _writer.write("\t\t" + _taskMap.get(task).getTaskID() + "\t\t[Weight="
                + _taskMap.get(task).getProcessTime()
                + ",Start=" + startTime
                + ",Processor=" + offsetProcID + "];\n");
    }

    /**
     * Used to write an edge to the output file.
     * @param parentTask the parent node corresponding to the edge.
     * @param childTask the child node corresponding to the edge.
     * @param weight the communication cost (weight) of the weight
     * @throws IOException if the edge or arguments are malformed.
     */
    private void writeEdge(Integer parentTask, Integer childTask, int weight) throws IOException {
        _writer.write("\t\t"  + _offsetTaskMap.get(parentTask).getTaskID() + " -> "
                + _taskMap.get(childTask).getTaskID() + "\t[Weight=" + weight + "];\n");
    }

    /**
     * Undoes the normalization of the task id that was done in the input parsing.
     * @param tasks tasks to be un-normalized.
     * @return the un-normalized set of tasks.
     */
    private Map<Integer, Task> convertTaskID(Map<Integer, Task> tasks) {
        Map<Integer, Task> newTaskMap = new HashMap<>();
        for (Integer offsetID : tasks.keySet()) {
            newTaskMap.put(tasks.get(offsetID).getTaskID(), tasks.get(offsetID));
        }
        return newTaskMap;
    }
}
