package swiftsolutions.unit.benchmark.test;

import org.junit.Before;
import org.junit.Test;

import swiftsolutions.unit.benchmark.BenchmarkParser;

import static junit.framework.Assert.assertTrue;

/**
 *Test the ability of the benchmarkParser to generate and catagorise files
 */
public class FolderReadBenchmark {
    
	BenchmarkParser _benchmarkParser;
	
	@Before
	public void generateBenchmark() {
		
		_benchmarkParser = new BenchmarkParser("src/test/resources/test_graphs_full_name");
		_benchmarkParser.catagoriseFiles();

		
	}
	
	@Test
    public void testExample() {
        
		_benchmarkParser.getNodesCatagory();
		
		
    }
}
