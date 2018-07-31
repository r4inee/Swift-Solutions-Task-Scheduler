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
            GraphParser parser = new GraphParser(
                    new FileInputStream("src/test/resources/test_graphs/Nodes_7_OutTree.dot"));
//            parser.getNodes().forEach((s, graphNode) -> {
//                System.out.println(graphNode.getAttribute("Weight"));
//                System.out.println(s);
//            });

            parser.getEdges().forEach((s, graphEdge) -> {
                System.out.println(graphEdge.getNode1());
                System.out.println("=>");
                System.out.println(graphEdge.getNode2());
                System.out.println(" ");
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }
    }
}
