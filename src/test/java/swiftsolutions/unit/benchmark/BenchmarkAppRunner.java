package swiftsolutions.unit.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.taskscheduler.branchandbound.BNBAlgorithm;

public class BenchmarkAppRunner {

	private ArrayList<File> _graphs;
	private DOTInputParser _inputParser;
	private static Map<String, Long> _outputs;
	private ArrayList<String> _timedOutGraphs;
	int _timeout;
	int _numCores;

	public BenchmarkAppRunner(int numCores) {
		
		_timedOutGraphs = new ArrayList<String>();
		_timeout = 5;
		_inputParser = new DOTInputParser();
		_numCores = numCores;
		_outputs =  new HashMap<String, Long>();
		_graphs = new ArrayList<File>();

	}

	public void addSingle(File file) {

		_graphs.add(file);

	}
	public void addList(ArrayList<File> files) {

		_graphs.addAll(files);

	}

	public void runAll() {

		List<Future<Schedule>> future;
		ExecutorService executor;

		for(File runnable : _graphs) {


			System.out.println("\nAttempting to run graph: " + runnable.getName());

			try {

				Map<Integer, Task> tasks = null;
				_inputParser = new DOTInputParser();
				try {
					tasks = _inputParser.parse(runnable.toString());
				} catch (InputException e) {
					e.printStackTrace();
				}
				executor = Executors.newSingleThreadExecutor();
				long start = System.currentTimeMillis();
				future = executor.invokeAll(Arrays.asList(new Runner(runnable, tasks, getProcs(runnable))), _timeout, TimeUnit.SECONDS);
				long end = System.currentTimeMillis();
				_outputs.put(runnable.getName(), end - start);

				try {
				System.out.println("graph:" + runnable.getName() + " ran in: " + (end - start) + "ms"  );
				System.out.println(future.get(0).get().getOutputString());
				}catch(Exception e) {
				System.out.println("no solution");
				}

				executor.shutdownNow();

				if(future.get(0).isCancelled()) {
					_timedOutGraphs.add(runnable.getName());
					System.out.println(runnable.getName() + " took more than " + _timeout + " seconds to run ");
				}
				
			}catch(Exception e) {
				e.printStackTrace();
				System.out.println("Graph run failed");

			}
		}

		System.out.println("\n!!done!!");
	}

	public Map<String, Long> getOutputs() {

		return _outputs;

	}
	
	public ArrayList<String> getTimedOutGraphs(){
		
		return _timedOutGraphs;

	}

	public int getProcs(File graph) {

		String split = graph.getName().split("_")[0].replaceAll("p", "");
		int processors = 4;
		try {

			processors = Integer.parseInt(split);

		}catch(Exception e) {
		}

		return processors;

	}

	static class Runner implements Callable<Schedule>
	{
		Map<Integer, Task> _tasks;
		int _processors;
		File _runnable;

		public Runner(File runnable, Map<Integer, Task> tasks, int processors) {

			_runnable = runnable;
			_processors = processors;
			_tasks = tasks;
		}

		public Schedule call() {

			
			Algorithm algorithm = new BNBAlgorithm();
			algorithm.setProcessors(_processors);
			Schedule outputSchedule = algorithm.execute(_tasks);
			return outputSchedule;
		}
	}

}
