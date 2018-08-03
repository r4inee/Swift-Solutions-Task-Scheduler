package swiftsolutions.unit.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import swiftsolutions.cli.CLIArgumentParser;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.output.AppOutputManager;
import swiftsolutions.output.DOTOutputWriter;
import swiftsolutions.output.OutputMessage;
import swiftsolutions.output.OutputType;
import swiftsolutions.taskscheduler.Algorithms;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.SchedulingAlgorithmFactory;
import swiftsolutions.taskscheduler.Task;

public class BenchmarkAppRunner {


	private ArrayList<File> _graphs;
	private DOTInputParser _inputParser;
	private SchedulingAlgorithmFactory _algorithmFactory;
	private Map<String, Long> _outputs;

	int _numProcessors;
	int _numCores;

	public BenchmarkAppRunner(int numProcessors, int numCores) {

		_algorithmFactory = new SchedulingAlgorithmFactory();
		_inputParser = new DOTInputParser();
		_numProcessors = numProcessors;
		_numCores = numCores;
		_outputs =  new HashMap<String, Long>();
		_graphs = new ArrayList<File>();
		
	}

	public void addSingle(File file) {

		_graphs.add(file);

	}
	public void addList(ArrayList<File> files) {
		
		System.out.println(files + " were added");
		_graphs.addAll(files);

	}

	public void run() {

		for(File runnable : _graphs) {

			
			System.out.println("Attempting to run graph: " + runnable.getName());

			try {
			
			Set<Task> tasks = null;
			_inputParser = new DOTInputParser();
			try {
				tasks = _inputParser.parse(runnable.toString());
			} catch (InputException e) {
				e.printStackTrace();
			}
			long start = System.currentTimeMillis();
			Algorithm algorithm = _algorithmFactory.getAlgorithm(Algorithms.BRANCH_AND_BOUND, _numProcessors, _numCores);
			Schedule outputSchedule = algorithm.execute(tasks);
			long end = System.currentTimeMillis();
			_outputs.put(runnable.getName(), end - start);
				
				System.out.println("graph:" + runnable.getName() + " ran in" + (end - start));
			
			}catch(Exception e) {
				
				System.out.println("Graph run failed");
				
			}
		}
		
		System.out.println("!!done!!");
	}

	public Map<String, Long> getOutputs() {

		return _outputs;

	}


}
