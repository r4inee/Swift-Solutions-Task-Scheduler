package swiftsolutions.unit.benchmark.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import swiftsolutions.unit.benchmark.BenchmarkAppRunner;
import swiftsolutions.unit.benchmark.BenchmarkParser;

import static junit.framework.Assert.assertTrue;

/**
 *Test the ability of the benchmarkParser to generate and catagorise files
 */
public class GraphRunnerBenchmark {
    
	BenchmarkParser _benchmarkParser;
	BenchmarkAppRunner _runner;
	
	@Before
	public void generateBenchmark() throws Exception {
		
		_benchmarkParser = new BenchmarkParser("src/test/resources/test_graphs_full_name/");
		_benchmarkParser.catagoriseFiles();
		_runner = new BenchmarkAppRunner(1);

		
	}
	
	@Test
    public void runTest() {
        
		ArrayList<File> nodeGraphs10 = _benchmarkParser.getNodesCatagory().get("10");
		ArrayList<File> allGraphs = _benchmarkParser.getAllGraphs();
		//_runner.addSingle(new File("C:/Users/User/Documents/uni/306/project1/SOFTENG306_Project1/src/test/resources/test_graphs_full_name/16p_Fork_Join_Nodes_10_CCR_0.10_WeightType_Random.dot"));
		//_runner.addSingle(new File("C:/Users/User/Documents/uni/306/project1/SOFTENG306_Project1/src/test/resources/test_graphs_full_name/16p_Fork_Join_Nodes_21_CCR_0.10_WeightType_Random.dot"));
		_runner.addList(allGraphs);
		_runner.runAll();
		System.out.println(_runner.getOutputs().toString());
		assertEquals(allGraphs.size() , _runner.getOutputs().size());
    }
}
