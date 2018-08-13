package swiftsolutions.unit.benchmark.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.unit.benchmark.BenchmarkAppRunner;
import swiftsolutions.unit.benchmark.BenchmarkParser;

import static junit.framework.Assert.assertTrue;

/**
 *Test the ability of the benchmarkParser to generate and catagorise files
 */
public class GraphRunnerBenchmark {
    

	/*
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
		
		//test each graph gave correct outputs
		System.out.println(_runner.getOutputs().keySet());
		
		
    }
	
	/*
	 * Used to run all full name graphs
	 */
	
	@Test
    public void runFullNameGraphs() {
		
		BenchmarkAppRunner _runner = new BenchmarkAppRunner(1);
		BenchmarkParser _benchmarkParser = new BenchmarkParser("src/test/resources/test_graphs_full_name/");
		_benchmarkParser.catagoriseFiles();
		ArrayList<File> nodeGraphs10 = _benchmarkParser.getNodesCatagory().get("21");
		ArrayList<File> allGraphs = _benchmarkParser.getAllGraphs();
		//_runner.addSingle(new File("C:/Users/User/Documents/uni/306/project1/SOFTENG306_Project1/src/test/resources/test_graphs_full_name/16p_Fork_Join_Nodes_10_CCR_0.10_WeightType_Random.dot"));
		//_runner.addSingle(new File("C:/Users/User/Documents/uni/306/project1/SOFTENG306_Project1/src/test/resources/test_graphs_full_name/16p_Fork_Join_Nodes_21_CCR_0.10_WeightType_Random.dot"));
		_runner.addList(nodeGraphs10);
		//_runner.runAll();
		//System.out.println(_runner.getOutputs().toString());
    }
}
