package swiftsolutions.unit.benchmark;

import swiftsolutions.interfaces.output.OutputManager;
import swiftsolutions.output.AppOutputManager;
import swiftsolutions.output.OutputMessage;
import swiftsolutions.output.OutputType;
import swiftsolutions.taskscheduler.branchandbound.BNBAlgorithm;
import swiftsolutions.taskscheduler.branchandboundastar.BBAAlgorithm;
import swiftsolutions.taskscheduler.branchandboundastarparallel.BBAAlgorithmParallel;
import swiftsolutions.unit.benchmark.BenchmarkAppRunner;
import swiftsolutions.unit.benchmark.BenchmarkParser;

import java.util.*;

/**
 * Class that compares two algorithms for run time.
 */
public class ComparisonBenchmark {

    /**
     * Argument contains the directory that will contains the graphs for the comparator to run.
     */
    public static final String FOLDER_PATH = "src/test/resources/test_graphs_full_name";

    public static final List<Class<?>> ALGORITHMS = new ArrayList<>();
    public static final int PROCESSORS = 2;
    public static final Map<Class<?>, List<RunnerInformation>> OUTPUT = new HashMap<>();

    public static void main(String[] args) {

        /**
         * Add the algorithms that the comparator will compare against.
         */
        ALGORITHMS.add(BNBAlgorithm.class);
        ALGORITHMS.add(BBAAlgorithm.class);

        OutputManager outputManager = new AppOutputManager();

        if (ALGORITHMS.size() == 0) {
            outputManager.send(new OutputMessage(OutputType.STATUS, "No Algorithms!"));
        }

        for (Class<?> algorithm : ALGORITHMS) {
            // Run the benchmark on each algorithm
            BenchmarkParser benchmarkParser = new BenchmarkParser(FOLDER_PATH);
            benchmarkParser.catagoriseFiles();

            BenchmarkAppRunner runner = new BenchmarkAppRunner(PROCESSORS);
            outputManager.send(new OutputMessage(OutputType.STATUS, "Running " + algorithm.getName() + "\n"));
            runner.addList(benchmarkParser.getAllGraphs());
            runner.setVerbose(false);
            runner.runAll();

            runner.setVerbose(true);
            Map<String, Long> runtimes = runner.getRunTimes();

            List<RunnerInformation> information = new ArrayList<>();

            Set<String> keys = runtimes.keySet();

            // Collect the information and output it
            keys.forEach((key) -> {

                    information.add(new RunnerInformation(algorithm, runtimes.get(key)));

                    outputManager.send(new OutputMessage(OutputType.STATUS,
                            "Ran " + key + "\n" +
                                    "---------------------------------------------\n" +
                                    "Runtime: " + runtimes.get(key) + "\n"

                    ));
            });

            OUTPUT.put(algorithm, information);
        }

        // Parse output data
        Map<Class<?>, AlgorithmStatistics> algorithmStatisticsMap = new HashMap<>();

        for (Class<?> algorithm : ALGORITHMS) {
            algorithmStatisticsMap.put(algorithm, new AlgorithmStatistics());
        }

        int graphsRun = OUTPUT.get(ALGORITHMS.get(0)).size();

        for (int i = 0; i < graphsRun; i++) {
            double bestRuntime = Integer.MAX_VALUE;

            Class<?> bestRuntimeClass = null;


            for (Class<?> algorithm : ALGORITHMS) {
                RunnerInformation info = OUTPUT.get(algorithm).get(i);

                if (info.getRunTime() <= bestRuntime) {
                    bestRuntime = info.getRunTime();
                    bestRuntimeClass = info.getAlgorithm();
                }
            }

            algorithmStatisticsMap.get(bestRuntimeClass).runtimeBest();
        }

        // Display results
        algorithmStatisticsMap.forEach((algorithm, info) -> {
            outputManager.send(new OutputMessage(OutputType.SUCCESS, String.format(
                    algorithm.getName() + "\n" +
                            "-------------------------------------------\n" +
                            "Best Runtime: %.2f%%\n"
            , ((double)info.getRuntimeBest()/(double)graphsRun) * 100)));
        });


    }

    private static class AlgorithmStatistics {
        private int _runtimeBest;

        public void runtimeBest() {
            _runtimeBest++;
        }

        public int getRuntimeBest() {
            return _runtimeBest;
        }
    }

    private static class RunnerInformation {


        private double _runTime;

        private Class<?> _algorithm;

        public RunnerInformation(Class<?> algorithm, double runTime) {
            _runTime = runTime;


            _algorithm = algorithm;
        }

        public double getRunTime() {
            return _runTime;
        }

        public Class<?> getAlgorithm() {
            return _algorithm;
        }
    }


}
