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
    }

    /**
     * This is a method which check if the file exist in the location supplied in the constructor or setter.
     * @return A set of object representing each of the task needed to be scheduled.
     * @throws InputException
     */
    @Override
    public Set<Task> parse() throws InputException {
        allTasks = new HashMap<>();
        try {
            // Using the digraph parser tool to parse the file.
            this.parser = new GraphParser(new FileInputStream(this.filename));
            this.inputNodes = this.parser.getNodes();
            this.inputEdges = this.parser.getEdges();
        } catch (FileNotFoundException e) {
            throw new InputException("Input graph file not found");
        }

        parseNodes();
        parseEdges();

        return new HashSet<>(allTasks.values());
    }

    /**
     * This is a method which parses the GraphNode objects from the input .dot file.
     * @throws InputException
     */
    private void parseNodes() throws InputException {
        // Looping through each of the nodes
        for (String nodeName : this.inputNodes.keySet()) {
            Task task;
            try {
                int id = Integer.parseInt(nodeName);
                int weight = Integer.parseInt(this.inputNodes.get(nodeName).getAttribute("Weight").toString());
                // Creating a new Task object and appending to the map with id as the key.
                task = new Task(id, weight);
                allTasks.put(id, task);
            } catch (NumberFormatException e) {
                throw new InputException("Input graph could not be parsed correctly");
            }
        }
    }

    /**
     * This is a method which parses the GraphEdge objects from the input .dot file.
     * @throws InputException
     */
    private void parseEdges() throws InputException {
        // Looping through each of the nodes
        for (GraphEdge edge : this.inputEdges.values()) {
            try {
                int sourceNode = Integer.parseInt(edge.getNode1().getId());
                int dstNode = Integer.parseInt(edge.getNode2().getId());
                if ((allTasks.keySet().contains(sourceNode))
                        && (allTasks.keySet().contains(dstNode))) {
                    Task parent = allTasks.get(sourceNode);
                    Task child = allTasks.get(dstNode);
                    // Inserting the parent to child dependency.
                    int weight = Integer.parseInt(edge.getAttribute("Weight").toString());
                    parent.addChild(child, weight);
                } else {
                    throw new InputException("Input graph could not be parsed correctly");
                }
            } catch (NumberFormatException e) {
                throw new InputException("Input graph could not be parsed correctly");
            }
        }
    }
}
