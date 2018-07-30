package swiftsolutions.unit;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;


/**
 * This contains basic unit tests for testing parsing of graph data from different input formats.
 */

public class InputParserTest {

    @Test
    public void testMain() {
        try {
            GraphParser parser = new GraphParser(new FileInputStream("src/test/resources/test.dg"));
            Map<String, GraphNode> nodes = parser.getNodes();

            Map<String, GraphEdge> edges = parser.getEdges();

            assertEquals(4, nodes.values().size());
            assertEquals(4, edges.values().size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }
    }
}
