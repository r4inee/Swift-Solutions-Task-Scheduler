package swiftsolutions.unit.benchmark.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import swiftsolutions.unit.benchmark.BenchmarkParser;

/**
 * Test the ability of the benchmarkParser to generate and catagorise files
 */
public class FolderReadBenchmark {
    
	BenchmarkParser _benchmarkParser;
	
	@Before
	public void generateBenchmark() {
		// Initialize the parser.
		_benchmarkParser = new BenchmarkParser("src/test/resources/test_graphs_full_name");
		_benchmarkParser.catagoriseFiles();
	}
	
	@Test
    public void testExample() {
        // Assert that information matches up with the corresponding expected information.
		assertEquals( 1 , _benchmarkParser.getNodesCategory().keySet().size());
		assertEquals( 2 , _benchmarkParser.getProcessorCategory().keySet().size());
		assertEquals( 5 , _benchmarkParser.getTypeCategory().keySet().size());
    }
}
