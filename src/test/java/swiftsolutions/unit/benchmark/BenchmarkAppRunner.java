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
	int _timeout = 5;
	int _numCores;

	public BenchmarkAppRunner(int numCores) {

		_inputParser = new DOTInputParser();
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

	public void runAll() {

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

				ExecutorService executor = Executors.newSingleThreadExecutor();
				List<Future<String>> future = executor.invokeAll(Arrays.asList(new Runner(runnable, tasks, getProcs(runnable))), _timeout, TimeUnit.SECONDS); 
				executor.shutdown();
				if(future.get(0).isCancelled()) {
					System.out.println(runnable.getName() + " took more than " + _timeout + " seconds to run ");
				}

			}catch(Exception e) {
				e.printStackTrace();
				System.out.println("Graph run failed");

			}
		}

		System.out.println("!!done!!");
	}

	public Map<String, Long> getOutputs() {

		return _outputs;

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

	static class Runner implements Callable<String>
	{
		Map<Integer, Task> _tasks;
		int _processors;
		File _runnable;

		public Runner(File runnable, Map<Integer, Task> tasks, int processors) {

			_runnable = runnable;
			_processors = processors;
			_tasks = tasks;
		}

		public String call() {

			long start = System.currentTimeMillis();
			Algorithm algorithm = new BNBAlgorithm();
			algorithm.setProcessors(_processors);
			System.out.println(_tasks.toString());
			Schedule outputSchedule = algorithm.execute(_tasks);
			long end = System.currentTimeMillis();

			_outputs.put(_runnable.getName(), end - start);
			System.out.println(outputSchedule.getOutputString());

			System.out.println("graph:" + _runnable.getName() + " ran in: " + (end - start) + "ms"  );
			return "";
		}
	}

}
