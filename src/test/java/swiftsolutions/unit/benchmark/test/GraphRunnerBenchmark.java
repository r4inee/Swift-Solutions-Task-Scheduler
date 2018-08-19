package swiftsolutions.unit.benchmark.test;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.ArrayList;
import org.junit.Test;
import swiftsolutions.unit.benchmark.BenchmarkAppRunner;
import swiftsolutions.unit.benchmark.BenchmarkParser;

/**
 * Test the ability of the benchmarkParser to generate and catagorize files
 */
public class GraphRunnerBenchmark {
    

	/**
	 * Used to run all all lecturer provided graphs
	 */
	@Test
    public void runShortName() {
		
		BenchmarkAppRunner _runner = new BenchmarkAppRunner(1);
		BenchmarkParser _benchmarkParser = new BenchmarkParser("src/test/resources/test_graphs/");
		
		
		ArrayList<File> allGraphs = _benchmarkParser.getAllGraphs();
		_runner.addList(allGraphs);
		_runner.runAll();
		assertEquals(allGraphs.size() , _runner.getOutputs().size());
		assertEquals(0, _runner.getTimedOutGraphs().size());
		
		// Test each graph gave correct outputs
		System.out.println(_runner.getOutputs().keySet());
		
		
    }
	
	/**
	 * Used to run all full name graphs
	 */
	@Test
    public void runFullNameGraphs() {
		
		BenchmarkAppRunner _runner = new BenchmarkAppRunner(2);

		BenchmarkParser _benchmarkParser = new BenchmarkParser("src/test/resources/test_graphs_full_name/");
		_benchmarkParser.catagoriseFiles();
		
		ArrayList<File> nodeGraphs10 = _benchmarkParser.getNodesCategory().get("10");
		_runner.addList(nodeGraphs10);
		_runner.runAll();
		System.out.println("Passed graphs: "+ _runner.getOutputs().toString());
		System.out.println("Timed Out Graphs: " + _runner.getTimedOutGraphs());
    }
}
