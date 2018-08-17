package swiftsolutions.unit.benchmark.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import swiftsolutions.unit.benchmark.BenchmarkParser;

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
        
		assertEquals( 3 , _benchmarkParser.getNodesCatagory().keySet().size());
		assertEquals( 4 , _benchmarkParser.getProcessorCatagory().keySet().size());
		assertEquals( 14 , _benchmarkParser.getTypeCatagory().keySet().size());
		for (File files : _benchmarkParser.getTypeCatagory().get("Fork")) {
			
			System.out.println(files.getName());
			
		}
		

    }
}
