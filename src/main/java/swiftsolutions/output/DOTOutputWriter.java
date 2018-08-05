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
import java.util.Map;

public class DOTOutputWriter implements OutputWriter {

    private BufferedWriter _writer;
    private Map<Integer, Task> _taskMap;

    @Override
    public void serialize(String filename, Schedule schedule, Map<Integer, Task> taskMap) throws OutputException {
        _taskMap = taskMap;
        String outputFile = new File(filename).getName();
        String outputFileName = new File(filename).getName();
        if (outputFile.length() > 1) {
            outputFile = outputFile.substring(0, 1).toUpperCase() + outputFile.substring(1);
        } else {
            outputFile = outputFile.toUpperCase();
        }

        if(outputFile.lastIndexOf(".") != -1 && outputFile.lastIndexOf(".") != 0) {
            outputFile = outputFile.substring(0, outputFile.lastIndexOf("."));
            outputFileName = outputFileName.substring(0, outputFileName.lastIndexOf("."));
        }

        try {
            _writer = new BufferedWriter(new FileWriter(outputFileName + "-output.dot"));
            _writer.write("digraph \"output" + outputFile + "\" {\n");

            for (Integer task : taskMap.keySet()) {
                Pair<Integer, Integer> proc = schedule.getProcessor(task);
                writeNode(task, proc.getA(), proc.getB());
            }
            for (Integer child : taskMap.keySet()) {
                for (Integer parent : _taskMap.get(child).getParentTasks()) {
                    writeEdge(parent, child, taskMap.get(child).getCommunicationCosts(parent));
                }

            }
            _writer.write("}");
            _writer.close();
        } catch (IOException e) {
            throw new OutputException("Error occur when writing output to file");
        }

    }

    private void writeNode(Integer task, int processorID, int startTime) throws IOException {
        int offsetProcID = processorID+1;
        _writer.write("\t\t" + _taskMap.get(task).getTaskID() + "\t\t[Weight="
                + _taskMap.get(task).getProcessTime()
                + ",Start=" + startTime
                + ",Processor=" + offsetProcID + "];\n");
    }

    private void writeEdge(Integer parentTask, Integer childTask, int weight) throws IOException {
        _writer.write("\t\t"  + _taskMap.get(parentTask).getTaskID() + " -> "
                + _taskMap.get(childTask).getTaskID() + "\t[Weight=" + weight + "];\n");
    }
}
