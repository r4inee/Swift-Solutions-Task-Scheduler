package swiftsolutions.unit.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.sun.management.OperatingSystemMXBean;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.interfaces.output.OutputManager;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.output.AppOutputManager;
import swiftsolutions.output.OutputMessage;
import swiftsolutions.output.OutputType;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.taskscheduler.branchandboundastarparallel.BBAAlgorithmParallel;
import swiftsolutions.taskscheduler.branchandbound.BNBAlgorithm;
import swiftsolutions.taskscheduler.branchandboundastar.BBAAlgorithm;
import swiftsolutions.util.Pair;

/**
 *  Class used to automate and run multiple graphs at once, keeps execution logistics in fields.
 */
public class BenchmarkAppRunner {

	/**
	 * 	A queue of all graphs to run.
	 */
	private ArrayList<File> _graphs;
	private DOTInputParser _inputParser;
	private static Map<String, Pair<Schedule,Long>> _outputs;
	private ArrayList<String> _timedOutGraphs;
	private ArrayList<String> _nonOptimalGraphs;
	private ArrayList<String> _invalidGraphs;
	private Map<String, Long> _runTimes;
	private Class<?> _algorithm;
	private OutputManager _outputManager;
	private Map<String, List<Long>> _memoryUsage;
	private Map<String, List<Double>> _cpuUsage;

	int _timeout;
	int _numCores;

	public BenchmarkAppRunner(int numCores) {
		_algorithm = BBAAlgorithm.class;
		_nonOptimalGraphs = new ArrayList<>();
		_invalidGraphs = new ArrayList<>();
		_outputs = new HashMap<>();
		_timedOutGraphs = new ArrayList<>();
		_timeout = 20;
		_inputParser = new DOTInputParser();
		_numCores = numCores;
		_graphs = new ArrayList<>();
		_runTimes = new HashMap<>();
		_outputManager = new AppOutputManager();
		_memoryUsage = new HashMap<>();
		_cpuUsage = new HashMap<>();
	}

	/**
	 * Adds a single graph to be run.
	 * @param file of graph to be run.
	 */
	public void addSingle(File file) {
		_graphs.add(file);
	}

	/**
	 *  Add a list of graph files to run
	 * @param files graph files to be added
	 */
	public void addList(ArrayList<File> files) {
		_graphs.addAll(files);
	}

	/**
	 * Clears the list of graphs to be run.
	 */
	public void clearList() {
		_graphs.clear();
	}

	/**
	 * @param verbose whether the bench marker will output console messages.
	 */
	public void setVerbose(boolean verbose) {
		this._outputManager.setConsoleLog(verbose);
	}

	/**
	 * 	Method to run all graph files in queue
	 */
	public void runAll() {

		// Initialise a list of future tasks
		List<Future<Schedule>> future;

		// Create a executor service
		ExecutorService executor;


		// For all graphs in the queue _graphs
		for(File runnable : _graphs) {
			// Set up timers for load statistics
			Timer loadTimer = new Timer();
			OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			List<Long> freeMemory = new ArrayList<>();
			List<Double> cpuLoad = new ArrayList<>();
			_outputManager.send(new OutputMessage(OutputType.STATUS, "\nAttempting to run graph: " + runnable.getName()));

			try {

				// Attempt to parse all graph files into tasks
				Map<Integer, Task> tasks = null;
				_inputParser = new DOTInputParser();
				try {
					tasks = _inputParser.parse(runnable.toString());
				} catch (InputException e) {
					e.printStackTrace();
				}

				// Create a new single thread executor
				executor = Executors.newSingleThreadExecutor();



				Runner runner = new Runner(runnable, tasks, getProcs(runnable));
				Algorithm algorithm = (Algorithm)_algorithm.newInstance();
				algorithm.setProcessors(getProcs(runnable));
				runner.setAlgorithm(algorithm);

				TimerTask timerTask = new TimerTask() {
					@Override
					public void run() {
						freeMemory.add(bean.getFreePhysicalMemorySize());
						cpuLoad.add(bean.getProcessCpuLoad());
					}
				};

				loadTimer.scheduleAtFixedRate(timerTask, 0 , 50);

				// Note start time, then run, then record stop time
				long start = System.currentTimeMillis();
				future = executor.invokeAll(Arrays.asList(runner), _timeout, TimeUnit.SECONDS);
				long end = System.currentTimeMillis();

				// Calculate memory usage
				long maxMemoryUsage = freeMemory.stream().mapToLong(val -> val).max().getAsLong();
				List<Long> memoryUsage = (freeMemory.stream().map(val -> maxMemoryUsage - val).collect(Collectors.toList()));
				_memoryUsage.put(runnable.getName(), memoryUsage);
				_cpuUsage.put(runnable.getName(), cpuLoad);

				loadTimer.cancel();
				// Try record execution logistics in fields, if execution was succesful
				// All logistic analysis after algorithm execution goes in here
				try {

					Schedule schedule = future.get(0).get();
					// Do a valid schedule check
					boolean scheduleValid = checkScheduleValidity(schedule,tasks);

					// Save the End time of the schedule
					int maxEndTime = 0;
					for (Integer task : schedule.getTaskToProcessorMap().keySet()) {

						int endTime = schedule.getTaskToProcessorMap().get(task).getB() + tasks.get(task).getProcessTime();
						if (endTime > maxEndTime) {
							maxEndTime = endTime;
						}
					}

					// Print run time
					_outputManager.send(new OutputMessage(OutputType.STATUS, "graph:" + runnable.getName() + " ran in: " + (end - start) + "ms"  ));

					// Remap tasks with correct ids
					schedule.convertTaskID(tasks);

					// Print schedule
					_outputManager.send(new OutputMessage(OutputType.STATUS, schedule.getOutputString()));

					// Print if schedule is not valid, throw exception, add graph to arraylist
					if(!scheduleValid) {

						_invalidGraphs.add(runnable.getName());
						throw new InvalidScheduleException("!!!schedule produced by: " + runnable.getName() + " was not valid!!!!");

					}else {

						_outputManager.send(new OutputMessage(OutputType.STATUS, "schedule is Valid"));

					}


					// Check if schedule is optimal using data saved in certain graph dot files
					// Check if file is a "long name file", containing optimal time info
					if(runnable.getName().split("_")[0].contains("p")) {

						// Open up file
						Scanner sc = new Scanner(runnable);

						int optimalRT = -1;

						// Find line which has "Total schedule length saved"
						while(sc.hasNextLine()) {

							String line = sc.nextLine();
							if(line.contains("Total schedule length")) {

								// If such line exists, parse the number into integer, then save it
								optimalRT = Integer.parseInt(line.replaceAll("[^\\d.]", ""));

								break;

							}

						}

						sc.close();

						
						if(optimalRT != -1) {
							
							// If out lastEndTime is not the optimalRT, throw an exception and save graph name to arraylist
							if(maxEndTime != optimalRT) {

								_nonOptimalGraphs.add(runnable.getName());
								throw new NonOptimalScheduleException("\n!!!schedule produced by: " + runnable.getName() + " Did not produce optimal schedule"
										+ "\n Our optimal Time: " + maxEndTime + "|| correct optimal Time: " + optimalRT);


							}else {

								_outputManager.send(new OutputMessage(OutputType.STATUS, "schedule was Optimal"));

							}
						}
					}

					_runTimes.put(runnable.getName(), (end - start));

					// Save schedule + run time
					_outputs.put(runnable.getName(), new Pair<>(future.get(0).get() , (end - start)));


				}catch(NonOptimalScheduleException e) {

					e.printStackTrace();
				}catch(InvalidScheduleException e) {

					e.printStackTrace();

				}
				catch(Exception e) {
					_outputManager.send(new OutputMessage(OutputType.STATUS, "no solution"));
				}

				// Shut down executor between loops
				executor.shutdownNow();

				// If task was cancelled, print timeout
				if(future.get(0).isCancelled()) {
					_timedOutGraphs.add(runnable.getName());
					_outputManager.send(new OutputMessage(OutputType.STATUS, runnable.getName() + " took more than " + _timeout + " seconds to run "));
				}

			}catch(Exception e) {
				_outputManager.send(new OutputMessage(OutputType.STATUS, "Graph run failed"));

			}

			_outputManager.send(new OutputMessage(OutputType.STATUS, "-------------------------------------------------------------------------------------------------"));
		}

		_outputManager.send(new OutputMessage(OutputType.STATUS, "\n!!done!!"));
	}

	/**
	 * Checks a schedules validity
	 * @param schedule schedule to be checked for validity
	 * @param tasks task information of the schedule
	 * @return whether the schedule is valid
	 */
	private boolean checkScheduleValidity(Schedule schedule, Map<Integer, Task> tasks) {

		Boolean scheduleValid = true;

		Map<Integer, Pair<Integer, Integer>> _taskToProcessorMap = schedule.getTaskToProcessorMap();

		Map<Integer, ArrayList<Integer>> procMap = new HashMap<>();
		for (Integer taskID : _taskToProcessorMap.keySet()) {
			Integer procID = _taskToProcessorMap.get(taskID).getA();
			if (!procMap.containsKey(procID)) {
				procMap.put(procID, new ArrayList<>());
			}
			procMap.get(procID).add(taskID);
		}
		List<Integer> processors = procMap.keySet().stream().collect(Collectors.toList());
		Collections.sort(processors);

		for (Integer procID : procMap.keySet()) {

			List<Pair<Integer, Integer>> taskStartTime = procMap.get(procID)
					.stream()
					.map((Integer task) -> {
						int startTime = _taskToProcessorMap.get(task).getB();
						return new Pair<>(task, startTime);
					}).collect(Collectors.toList());
			taskStartTime.sort(Comparator.comparing(Pair::getB));

			int lastTaskEndTime = 0;
			for (Pair<Integer, Integer> info: taskStartTime) {

				if( info.getB() < lastTaskEndTime) {

					scheduleValid = false;

				}
				lastTaskEndTime =  info.getB() + tasks.get(info.getA()).getProcessTime();

			}
		}

		return scheduleValid;

	}



	/**
	 * @return all graph schedule outputs
	 */
	public Map<String, Pair<Schedule, Long>> getOutputs() {

		return _outputs;

	}

	/**
	 * @return list of graphs that timed out
	 */
	public ArrayList<String> getTimedOutGraphs(){

		return _timedOutGraphs;

	}

	/**
	 * @return list of graphs with non optimal schedules
	 */
	public ArrayList<String> getNonOptimalSchedules(){
		return _nonOptimalGraphs;
	}

	/**
	 * @return list of graphs with invalid solutions
	 */
	public ArrayList<String> getInvalidSchedules(){

		return _invalidGraphs;

	}

	/**
	 * @param _algorithm the algorithm used
	 */
	public void setAlgorithm(Class<?> _algorithm) {
		this._algorithm = _algorithm;
	}

	/**
	 * @return the output run-times of the algorithms
	 */
	public Map<String, Long> getRunTimes() {
		return _runTimes;
	}

	/**
	 * @return the output manager
	 */
	public OutputManager getOutputManager() {
		return _outputManager;
	}

	/**
	 * @return the memory usage keyed by graph-name value is a list of the memory usage based off poll rate.
	 */
	public Map<String, List<Long>> getMemoryUsage() {
		return _memoryUsage;
	}

	/**
	 * @return cpu usage keyed by graph-name value is a list of cpu usage based off poll rate.
	 */
	public Map<String, List<Double>> getCpuUsage() {
		return _cpuUsage;
	}

	/**
	 * Used by executor class, checks graph file string to get appropriate number of processors, otherwise set to 4
	 * @param graph graph to be the processors of.
	 * @return number of processors to be run on the graph.
	 */
	private int getProcs(File graph) {

		String split = graph.getName().split("_")[0].replaceAll("p", "");
		int processors = 4;
		try {

			processors = Integer.parseInt(split);

		}catch(Exception e) {
		}

		return processors;

	}


	/**
	 * 	Static inner class used to handle execution of algorithms, called by runAll()
	 */
	static class Runner implements Callable<Schedule>
	{
		Map<Integer, Task> _tasks;
		int _processors;
		File _runnable;
		Algorithm _algorithm;

		public Runner(File runnable, Map<Integer, Task> tasks, int processors) {

			_algorithm = new BBAAlgorithm();
			_runnable = runnable;
			_processors = processors;
			_tasks = tasks;
		}

		/**
		 * @param _algorithm  the algorithm used by the runner
		 */
		public void setAlgorithm(Algorithm _algorithm) {
			this._algorithm = _algorithm;
		}

		/**
		 * Run the algorithm.
		 * @return schedule produced by the algorithm.
		 */
		public Schedule call() {
			_algorithm.setProcessors(_processors);
			Schedule outputSchedule = _algorithm.execute(_tasks);

			return outputSchedule;
		}
	}

}
