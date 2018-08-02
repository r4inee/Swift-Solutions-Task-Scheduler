package swiftsolutions.output;

import swiftsolutions.exceptions.OutputException;
import swiftsolutions.interfaces.output.OutputWriter;
import swiftsolutions.taskscheduler.Processor;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class DOTOutputWriter implements OutputWriter {

    @Override
    public void serialize(String filename, Schedule schedule) throws OutputException {

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            out.write("digraph \"output" + filename + "\" {\\n");
            Processor[] processor = schedule.getProcessors();
            for (int i = 0; i < processor.length; i++) {
                Map<Task, Pair<Long, Long>> processorSchedule = processor[i].getTaskList();
                Set<Task> taskSet = processorSchedule.keySet();
                for (int j = 0; j < taskSet.size()-1; j++) {

                }
                for (Task task : processorSchedule.keySet()) {
                    out.write("\t\t" + task.getTaskID() + "\t\t[Weight=" + task.getProcessTime() + ",Start=" +
                            processorSchedule.get(task).a + ",Processor=" + i+1 + "];");
                }

            }
            out.write("}");
            out.close();
        } catch (IOException e) {
            throw new OutputException("Error occur when writing output to file");
        }




    }
}
