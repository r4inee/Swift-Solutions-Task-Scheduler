package swiftsolutions.output;

import swiftsolutions.exceptions.OutputException;
import swiftsolutions.interfaces.output.OutputWriter;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class DOTOutputWriter implements OutputWriter {

    BufferedWriter writer;

    @Override
    public void serialize(String filename, Schedule schedule, Set<Task> tasks) throws OutputException {
        try {
            writer = new BufferedWriter(new FileWriter(filename));
            writer.write("digraph \"output" + filename + "\" {\\n");

            for (Task task : tasks) {
                Pair<Integer, Integer> proc = schedule.getProcessor(task);

            }

//            for (int i = 0; i < processor.length; i++) {
//                Map<Task, Pair<Long, Long>> processorSchedule = processor[i].getTaskList();
//                Set<Task> taskSet = processorSchedule.keySet();
//                int head = 1;
//                Task parentTask = null;
//
//
//
//
//
//
//                for (Iterator<Task> iter = taskSet.iterator(); iter.hasNext();) {
//                    Task task = iter.next();
//                    writeNode(childTask, i, processorSchedule.get(childTask));
//
//                    writeEdge(parentTask, childTask, delay);
//                    parentTask = childTask;
//                }
//
//            }
//            writer.write("}");
//            writer.close();
        } catch (IOException e) {
            throw new OutputException("Error occur when writing output to file");
        }

    }

    private void writeNode(Task task, int processorID, Pair<Long, Long> scheduledTime) throws IOException {
        writer.write("\t\t" + task.getTaskID() + "\t\t[Weight=" + task.getProcessTime()
                + ",Start=" + scheduledTime.getB() + ",Processor=" + processorID+1 + "];");
    }

    private void writeEdge(Task parentTask, Task childTask, long delay) throws IOException {
//        writer.write();
    }
}
