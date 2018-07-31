package swiftsolutions.input;

import com.paypal.digraph.parser.GraphParser;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.interfaces.Parser;
import swiftsolutions.taskscheduler.Task;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class DOTInputParser implements Parser {

    private String filename;

    public DOTInputParser(String filename) {
        this.filename = filename;
    }

    @Override
    public List<Task> parse(String filename) throws InputException {
        try {
            GraphParser parser = new GraphParser(new FileInputStream("src/test/java/swiftsolutions/unit/hi.dot"));
        } catch (FileNotFoundException e) {
            // debug mode handling file not found
            throw new InputException("Input graph file not found.");
        }





        return null;
    }
}
