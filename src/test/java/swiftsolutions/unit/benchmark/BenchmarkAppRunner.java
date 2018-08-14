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
import swiftsolutions.util.Pair;

/*
 * class used to automate and run multiple graphs at once
 * keeps execution logistics in fields
 */
public class BenchmarkAppRunner {

	//a queue of all graphs to run
	private ArrayList<File> _graphs;
	private DOTInputParser _inputParser;
	private static Map<String, Pair<Schedule,Long>> _outputs;
	private static Map<String, Map<Integer, Task>> _taskmaps;


	private ArrayList<String> _timedOutGraphs;
	int _timeout;
	int _numCores;

	public BenchmarkAppRunner(int numCores) {

		_taskmaps = new HashMap<String, Map<Integer, Task>>();
		_outputs = new HashMap<String, Pair<Schedule,Long>>();
		_timedOutGraphs = new ArrayList<String>();
		_timeout = 5;
		_inputParser = new DOTInputParser();
		_numCores = numCores;
		_graphs = new ArrayList<File>();

	}

	//adds a single graph to queue
	public void addSingle(File file) {

		_graphs.add(file);

	}

	//add a list of files to run
	public void addList(ArrayList<File> files) {

		_graphs.addAll(files);

	}

	//method to run all graph files in queue
	public void runAll() {

		//initialise a list of future tasks
		List<Future<Schedule>> future;

		//create a executor service
		ExecutorService executor;


		//for all graphs in the queue _graphs
		for(File runnable : _graphs) {


			System.out.println("\nAttempting to run graph: " + runnable.getName());

			try {

				//attempt to parse all graph files into tasks
				Map<Integer, Task> tasks = null;
				_inputParser = new DOTInputParser();
				try {
					tasks = _inputParser.parse(runnable.toString());
				} catch (InputException e) {
					e.printStackTrace();
				}

				//create a new single thread executor
				executor = Executors.newSingleThreadExecutor();
				
				//note start time, then run, then record stop time
				long start = System.currentTimeMillis();
				future = executor.invokeAll(Arrays.asList(new Runner(runnable, tasks, getProcs(runnable))), _timeout, TimeUnit.SECONDS);
				long end = System.currentTimeMillis();

				//try record execution logistics in fields, if execution was succesful
				try {
					System.out.println("graph:" + runnable.getName() + " ran in: " + (end - start) + "ms"  );
					System.out.println(future.get(0).get().getOutputString());
					_outputs.put(runnable.getName(), new Pair<Schedule,Long>(future.get(0).get() , (end - start)));
					_taskmaps.put(runnable.getName(), tasks);

				}catch(Exception e) {
					System.out.println("no solution");
				}
				
				//shut down executor between loops
				executor.shutdownNow();

				//if task was cancelled, print timeout
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

	//get all graph schedule outputs
	public Map<String, Pair<Schedule, Long>> getOutputs() {

		return _outputs;

	}

	//get all graph taskmaps
	public Map<String, Map<Integer, Task>> getTaskMaps() {

		return _taskmaps;

	}

	//get list of graphs that timed out
	public ArrayList<String> getTimedOutGraphs(){

		return _timedOutGraphs;

	}

	//used by executor class, checks graph file string to get appropriate number of processors, otherwise set to 4
	private int getProcs(File graph) {

		String split = graph.getName().split("_")[0].replaceAll("p", "");
		int processors = 4;
		try {

			processors = Integer.parseInt(split);

		}catch(Exception e) {
		}

		return processors;

	}

	//static inner class used to handle execution of algorithms, called by runAll()
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
			outputSchedule.convertTaskID(_tasks);

			return outputSchedule;
		}
	}

}
