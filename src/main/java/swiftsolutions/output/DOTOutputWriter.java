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
import java.util.Set;

public class DOTOutputWriter implements OutputWriter {

    private BufferedWriter _writer;

    @Override
    public void serialize(String filename, Schedule schedule, Set<Task> tasks) throws OutputException {
        try {
            String outputFile = new File(filename).getName();
            _writer = new BufferedWriter(new FileWriter(outputFile));
            _writer.write("digraph \"output" + outputFile + "\" {\n");

            for (Task task : tasks) {
                Pair<Integer, Integer> proc = schedule.getProcessor(task);
                writeNode(task, proc.getA(), proc.getB());
            }
            for (Task task : tasks) {
                for (Task parent : task.getParentTasks()) {
                    writeEdge(parent, task, parent.getCommunicationCosts(task.getTaskID()));
                }

            }
            _writer.write("}");
            _writer.close();
        } catch (IOException e) {
            throw new OutputException("Error occur when writing output to file");
        }

    }

    private void writeNode(Task task, int processorID, int startTime) throws IOException {
        int offsetProcID = processorID+1;
        _writer.write("\t\t" + task.getTaskID() + "\t\t[Weight=" + task.getProcessTime()
                + ",Start=" + startTime + ",Processor=" + offsetProcID + "];\n");
    }

    private void writeEdge(Task parentTask, Task childTask, int weight) throws IOException {
        _writer.write("\t\t"  + parentTask.getTaskID() + "->" + childTask.getTaskID() + "\t[Weight=" +
                weight + "];\n");
    }
}
