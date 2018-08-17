package swiftsolutions.unit.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import swiftsolutions.exceptions.InputException;
import swiftsolutions.input.DOTInputParser;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;
import swiftsolutions.taskscheduler.branchandbound.BNBAlgorithm;
import swiftsolutions.taskscheduler.brandandboundastar.BBAAlgorithm;
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
	private ArrayList<String> _timedOutGraphs;
	private ArrayList<String> _nonOptimalGraphs;
	private ArrayList<String> _invalidGraphs;

	int _timeout;
	int _numCores;

	public BenchmarkAppRunner(int numCores) {

		_nonOptimalGraphs = new ArrayList<String>();
		_invalidGraphs = new ArrayList<String>();
		_outputs = new HashMap<String, Pair<Schedule,Long>>();
		_timedOutGraphs = new ArrayList<String>();
		_timeout = 20;
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
				Schedule schedule = future.get(0).get();

				//try record execution logistics in fields, if execution was succesful
				//all logistic analysis after algorithm execution goes in here
				try {


					//do a valid schedule check
					boolean scheduleValid = checkScheduleValidity(schedule,tasks);

					//save the End time of the schedule
					int maxEndTime = 0;
					for (Integer task : schedule.getTaskToProcessorMap().keySet()) {

						int endTime = schedule.getTaskToProcessorMap().get(task).getB() + tasks.get(task).getProcessTime();
						if (endTime > maxEndTime) {
							maxEndTime = endTime;
						}
					}

					//print run time
					System.out.println("graph:" + runnable.getName() + " ran in: " + (end - start) + "ms"  );

					//Remap tasks with correct ids
					schedule.convertTaskID(tasks);

					//print schedule 
					System.out.println(schedule.getOutputString());

					//print if schedule is not valid, throw exception, add graph to arraylist
					if(!scheduleValid) {

						_invalidGraphs.add(runnable.getName());
						throw new InvalidScheduleException("!!!schedule produced by: " + runnable.getName() + " was not valid!!!!");

					}else {

						System.out.println("schedule is Valid");

					}


					//check if schedule is optimal using data saved in certain graph dot files
					//check if file is a "long name file", containing optimal time info
					if(runnable.getName().split("_")[0].contains("p")) {

						//open up file
						FileReader fr = new FileReader(runnable);
						BufferedReader bufferedReader = new BufferedReader(fr);
						Scanner sc = new Scanner(runnable);

						int optimalRT = -1;

						//find line which has "Total schedule length saved"
						while(sc.hasNextLine()) {

							String line = sc.nextLine();
							if(line.contains("Total schedule length")) {

								//if such line exists, parse the number into integer, then save it
								optimalRT = Integer.parseInt(line.replaceAll("[^\\d.]", ""));

								break;

							}

						}

						sc.close();

						
						if(optimalRT != -1) {
							
							//if out lastEndTime is not the optimalRT, throw an exception and save graph name to arraylist
							if(maxEndTime != optimalRT) {

								_nonOptimalGraphs.add(runnable.getName());
								throw new NonOptimalScheduleException("\n!!!schedule produced by: " + runnable.getName() + " Did not produce optimal schedule"
										+ "\n Our optimal Time: " + maxEndTime + "|| correct optimal Time: " + optimalRT);


							}else {

								System.out.println("schedule was Optimal");

							}
						}
					}

					//save schedule + run time
					_outputs.put(runnable.getName(), new Pair<Schedule,Long>(future.get(0).get() , (end - start)));


				}catch(NonOptimalScheduleException e) {

					e.printStackTrace();
				}catch(InvalidScheduleException e) {

					e.printStackTrace();

				}
				catch(Exception e) {
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
				System.out.println("Graph run failed");

			}

			System.out.println("-------------------------------------------------------------------------------------------------");
		}

		System.out.println("\n!!done!!");
	}

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
						return new Pair<Integer, Integer>(task, startTime);
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

	//get all graph schedule outputs
	public Map<String, Pair<Schedule, Long>> getOutputs() {

		return _outputs;

	}

	//get list of graphs that timed out
	public ArrayList<String> getTimedOutGraphs(){

		return _timedOutGraphs;

	}
	
	//get list of graphs with non optimal schedules
	public ArrayList<String> getNonOptimalSchedules(){

		return _nonOptimalGraphs;

	}
	
	//get list of graphs with invalid solutions
	public ArrayList<String> getInvalidSchedules(){

		return _invalidGraphs;

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

			Algorithm algorithm = new BBAAlgorithm();
			algorithm.setProcessors(_processors);
			Schedule outputSchedule = algorithm.execute(_tasks);

			return outputSchedule;
		}
	}

}
