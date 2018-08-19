package swiftsolutions.unit.benchmark.test;

import swiftsolutions.interfaces.output.OutputManager;
import swiftsolutions.output.AppOutputManager;
import swiftsolutions.output.OutputMessage;
import swiftsolutions.output.OutputType;
import swiftsolutions.taskscheduler.branchandbound.BNBAlgorithm;
import swiftsolutions.taskscheduler.branchandboundastar.BBAAlgorithm;
import swiftsolutions.unit.benchmark.BenchmarkAppRunner;
import swiftsolutions.unit.benchmark.BenchmarkParser;

import java.util.*;

/**
 * Class that compares two algorithms for run time, memory usage and cpu usage.
 */
public class ComparisonBenchmark {

    public static final List<Class<?>> ALGORITHMS = new ArrayList<>();
    public static final String FOLDER_PATH = "C:\\Users\\Winston\\Desktop\\asd";
    public static final int PROCESSORS = 2;
    public static final double BYTES_IN_MB = 1048576;
    public static final Map<Class<?>, List<RunnerInformation>> OUTPUT = new HashMap<>();

    public static void main(String[] args) {

        // Add algorithms to test
        ALGORITHMS.add(BBAAlgorithm.class);
        ALGORITHMS.add(BNBAlgorithm.class);

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
            Map<String, List<Long>> memoryUsage = runner.getMemoryUsage();
            Map<String, List<Double>> cpuUsage = runner.getCpuUsage();

            List<RunnerInformation> information = new ArrayList<>();

            Set<String> keys = runtimes.keySet();

            // Collect the information and output it
            keys.forEach((key) -> {
                double averageMemoryUsage = memoryUsage.get(key).stream().mapToLong(val -> val).average().getAsDouble();
                double maxMemoryUsage = memoryUsage.get(key).stream().mapToLong(val -> val).max().getAsLong();
                String averageMemoryUsageString = String.format("%.0f MB", averageMemoryUsage/BYTES_IN_MB);
                String maxMemoryUsageString = String.format("%.0f MB", maxMemoryUsage/BYTES_IN_MB);

                double averageCPUUsage = cpuUsage.get(key).stream().mapToDouble(val -> val).average().getAsDouble();
                double maxCPUUsage = cpuUsage.get(key).stream().mapToDouble(val -> val).max().getAsDouble();
                String averageCPUUsageString = String.format("%.2f%%", averageCPUUsage);
                String maxMemoryCPUString = String.format("%.2f%%", maxCPUUsage);

                information.add(new RunnerInformation(algorithm, key, runtimes.get(key), maxMemoryUsage, maxCPUUsage));

                outputManager.send(new OutputMessage(OutputType.STATUS,
                        "Ran " + key + "\n" +
                                "---------------------------------------------\n" +
                                "Runtime: " + runtimes.get(key) + "\n" +
                                "Memory Usage - Max: " + maxMemoryUsageString + " - Mean: " + averageMemoryUsageString + "\n" +
                                "CPU Usage - Max: " + maxMemoryCPUString + " - Mean: " + averageCPUUsageString + "\n"
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
            double bestCPU = Integer.MAX_VALUE;
            double bestMemory = Integer.MAX_VALUE;
            double bestRuntime = Integer.MAX_VALUE;
            Class<?> bestCPUClass = null;
            Class<?> bestMemoryClass = null;
            Class<?> bestRuntimeClass = null;


            for (Class<?> algorithm : ALGORITHMS) {
                RunnerInformation info = OUTPUT.get(algorithm).get(i);
                if (info.getMaxCPU() <= bestCPU) {
                    bestCPU = info.getMaxCPU();
                    bestCPUClass = info.getAlgorithm();
                }
                if (info.getMaxMemory() <= bestMemory) {
                    bestMemory = info.getMaxMemory();
                    bestMemoryClass = info.getAlgorithm();
                }
                if (info.getRunTime() <= bestRuntime) {
                    bestRuntime = info.getRunTime();
                    bestRuntimeClass = info.getAlgorithm();
                }
            }
            algorithmStatisticsMap.get(bestCPUClass).cpuBest();
            algorithmStatisticsMap.get(bestMemoryClass).memoryBest();
            algorithmStatisticsMap.get(bestRuntimeClass).runtimeBest();
        }

        // Display results
        algorithmStatisticsMap.forEach((algorithm, info) -> {
            outputManager.send(new OutputMessage(OutputType.SUCCESS, String.format(
                    algorithm.getName() + "\n" +
                            "-------------------------------------------\n" +
                            "Best Runtime: %.2f%%\n" +
                            "Best CPU Usage: %.2f%%\n" +
                            "Best Memory Usage: %.2f%%\n"
            , ((double)info.getRuntimeBest()/(double)graphsRun) * 100
                    , ((double)info.getCpuBest()/(double)graphsRun) * 100
                    , ((double)info.getMemoryBest()/(double)graphsRun) * 100)));
        });


    }

    private static class AlgorithmStatistics {
        private int _runtimeBest;
        private int _memoryBest;
        private int _cpuBest;

        public void cpuBest() {
             _cpuBest++;
        }

        public void memoryBest() {
            _memoryBest++;
        }

        public void runtimeBest() {
            _runtimeBest++;
        }

        public int getCpuBest() {
            return _cpuBest;
        }

        public int getMemoryBest() {
            return _memoryBest;
        }

        public int getRuntimeBest() {
            return _runtimeBest;
        }
    }

    private static class RunnerInformation {

        private String _graph;
        private double _runTime;
        private double _maxMemory;
        private double _maxCPU;
        private Class<?> _algorithm;

        public RunnerInformation(Class<?> algorithm, String graph, double runTime, double maxMemory, double maxCPU) {
            _runTime = runTime;
            _maxCPU = maxCPU;
            _maxMemory = maxMemory;
            _graph = graph;
            _algorithm = algorithm;
        }

        public double getMaxCPU() {
            return _maxCPU;
        }

        public double getMaxMemory() {
            return _maxMemory;
        }

        public double getRunTime() {
            return _runTime;
        }

        public String getGraph() {
            return _graph;
        }

        public Class<?> getAlgorithm() {
            return _algorithm;
        }
    }


}
