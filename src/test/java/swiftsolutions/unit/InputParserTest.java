package swiftsolutions.unit;

import com.paypal.digraph.parser.GraphParser;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import static junit.framework.Assert.fail;


/**
 * This contains basic unit tests for testing parsing of graph data from different input formats.
 */

public class InputParserTest {

    @Test
    public void testMain() {
        try {
            GraphParser parser = new GraphParser(new FileInputStream("src/test/java/swiftsolutions/unit/hi.dot"));
            System.out.println(parser.getGraphId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }
    }
}
