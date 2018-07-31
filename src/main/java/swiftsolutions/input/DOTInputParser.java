package swiftsolutions.input;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.interfaces.Parser;
import swiftsolutions.taskscheduler.Task;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class DOTInputParser implements Parser {

    private String filename;
    private GraphParser parser;
    private Map<Integer, Task> allTasks;
    private Map<String, GraphNode> inputNodes;
    private Map<String, GraphEdge> inputEdges;

    /**
     * Constructor which takes a filename in .dot format to parse.
     * @param filename
     */
    public DOTInputParser(String filename) {
        this.filename = filename;
        allTasks = new HashMap<>();
    }

    @Override
    public Set<Task> parse(String filename) throws InputException {
        try {
            this.parser = new GraphParser(new FileInputStream("src/test/java/swiftsolutions/unit/hi.dot"));
            this.inputNodes = this.parser.getNodes();
            this.inputEdges = this.parser.getEdges();
        } catch (FileNotFoundException e) {
            // debug mode handling file not found
            throw new InputException("Input graph file not found.");
        }

        parseNodes();
        parseEdges();

        return new HashSet<>(allTasks.values());
    }


    private void parseNodes() throws InputException {
        for (String nodeName : this.inputNodes.keySet()) {
            Task task;
            try {
                int id = Integer.parseInt(nodeName);
                int weight = Integer.parseInt(this.inputNodes.get(nodeName).getAttribute("Weight").toString());
                task = new Task(id, weight);
                allTasks.put(id, task);
            } catch (NumberFormatException e) {
                throw new InputException("Input graph could not be parsed.");
            }
        }
    }

    private void parseEdges() throws InputException {
        for (GraphEdge edge : this.inputEdges.values()) {
            if ((allTasks.keySet().contains(edge.getNode1().getId()))
                    && (allTasks.keySet().contains(edge.getNode2().getId()))) {
                Task parent = allTasks.get(edge.getNode1().getId());
                Task child = allTasks.get(edge.getNode2().getId());
                try {
                    int weight = Integer.parseInt(edge.getAttribute("Weight").toString());
                    parent.addChild(child, weight);
                } catch (NumberFormatException e) {
                    throw new InputException("Input graph could not be parsed.");
                }
            } else {
                throw new InputException("Input graph could not be parsed.");
            }
        }
    }
}
