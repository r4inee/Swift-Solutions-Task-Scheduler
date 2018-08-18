package swiftsolutions.gui;

import com.sun.management.OperatingSystemMXBean;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import swiftsolutions.Scheduler;
import swiftsolutions.interfaces.taskscheduler.VisualAlgorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Function;

/**
 * Controller class for the GUI
 */
public class GUIController {
    @FXML
    private Text time;
    @FXML
    private Button stop;
    @FXML
    private Button start;
    @FXML
    private Button write;
    @FXML
    private LineChart<Number, Number> memoryChart;
    @FXML
    private NumberAxis memoryX;
    @FXML
    private NumberAxis memoryY;
    @FXML
    private LineChart<Number, Number> cpuChart;
    @FXML
    private NumberAxis cpuX;
    @FXML
    private NumberAxis cpuY;
    @FXML
    private Text branches;
    @FXML
    private Text bound;
    @FXML
    private Text validSchedules;
    @FXML
    private Text pruned;
    @FXML
    private HBox topBar;
    @FXML
    private TableView<List<Integer>> scheduleTable;
    @FXML
    private TableColumn<List<Integer>, Integer> nodeIdCol;
    @FXML
    private TableColumn<List<Integer>, Integer> startTimeCol;
    @FXML
    private TableColumn<List<Integer>, Integer> endTimeCol;
    @FXML
    private TableColumn<List<Integer>, Integer> processorCol;
    @FXML
    private StackedBarChart<String, Number> barChart;
    @FXML
    private CategoryAxis barX;
    @FXML
    private NumberAxis barY;

    public static final String GRAY_COLOR = "#676767";
    public static final String RED_COLOR = "#e53935";
    public static final String YELLOW_COLOR = "#FB8C00";
    public static final String GREEN_COLOR = "#46bb7c";

    public static final int START_TIME = 0;
    public static final int END_TIME = 1;
    public static final int NODE_ID = 2;
    public static final int PROC_TIME = 3;
    public static final int IDLE_NODE_ID = -1;
    public static final int TASK_LENGTH = 4;

    public static final String TOP_BAR_BEFORE_START = "-fx-background-color: " + GRAY_COLOR;
    public static final String TOP_BAR_RUNNING = "-fx-background-color: " + YELLOW_COLOR;
    public static final String TOP_BAR_STOPPED = "-fx-background-color: " + RED_COLOR;
    public static final String TOP_BAR_SUCCESS = "-fx-background-color: " + GREEN_COLOR;

    public static final String BAR_LABEL_STYLE = "-fx-fill: white";
    public static final String BAR_IDLE = "-fx-bar-fill: transparent";
    public static final String BAR_TASK = "-fx-bar-fill: " + GREEN_COLOR + "; -fx-background-insets: 1, 0, 0, 1";
    public static final String BAR_X_LABEL = "Processors";
    public static final String BAR_Y_LABEL = "Time";

    public static final String APPLICATION_ICON_PATH = "template/icon.png";
    public static final String ALGORITHM_SUCCESS_ICON = "template/checked.png";
    public static final String WRITE_SUCCESS_ICON = "template/notepad.png";
    public static final String ALGORITHM_SUCCESS_DIALOG_TITLE = "Algorithm Successfully Executed!";
    public static final String ALGORITHM_SUCCESS_DIALOG_BODY = "The algorithm has finished running without any problems!";
    public static final String WRITE_SUCCESS_DIALOG_TITLE = "Output file successfully written!";
    public static final String WRITE_SUCCESS_DIALOG_BODY = "The output .dot file has been written to destination!";
    public static final int DIALOG_PADDING = 20;
    public static final int DIALOG_ICON_SIZE = 30;

    public static final String TABLE_PLACEHOLDER_TEXT = "No Schedules Found";

    public static final int POLL_INTERVAL = 10;
    public static final int TIMER_UPDATE_INTERVAL = 5;
    public static final int LOAD_UPDATE_INTERVAL = 5;

    private long startTime;
    private Timer timerTimer;
    private Timer pollTimer;
    private List<Timer> loadTimers;
    private VisualAlgorithm thread;
    private Scheduler scheduler;
    private boolean finished;
    private Map<Integer, Task> taskMap;

    /**
     * Sets the algorithm to be run
     * @param thread algorithm to be run
     */
    public void setAlgorithmThread(VisualAlgorithm thread) {
        this.thread = thread;
    }

    /**
     * Sets the scheduler
     * @param scheduler
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Sets the task information map
     * @param taskMap
     */
    public void setTaskMap(Map<Integer, Task> taskMap) {
        this.taskMap = taskMap;
    }

    /**
     * Function that runs are view has been initialized see JavaFX documentation
     */
    @FXML
    private void initialize() {
        // Initial Value
        finished = false;

        loadTimers = new ArrayList<>();

        // Set top bar color to gray
        topBar.setStyle(TOP_BAR_BEFORE_START);

        // Disable stop and write buttons
        stop.setDisable(true);
        write.setDisable(true);

        // Add button listeners
        start.setOnMouseClicked((MouseEvent event) -> start());
        stop.setOnMouseClicked((MouseEvent event) -> stop());
        write.setOnMouseClicked(event -> write());

        // Initialize schedule table
        scheduleTable.setPlaceholder(new Label(TABLE_PLACEHOLDER_TEXT));

        // Set up column based off input data
        startTimeCol.setCellValueFactory((TableColumn.CellDataFeatures<List<Integer>, Integer> param) ->
                new ReadOnlyObjectWrapper<>(param.getValue().get(0)));
        endTimeCol.setCellValueFactory((TableColumn.CellDataFeatures<List<Integer>, Integer> param) ->
                new ReadOnlyObjectWrapper<>(param.getValue().get(1)));
        processorCol.setCellValueFactory((TableColumn.CellDataFeatures<List<Integer>, Integer> param) ->
                new ReadOnlyObjectWrapper<>(param.getValue().get(2)));
        nodeIdCol.setCellValueFactory((TableColumn.CellDataFeatures<List<Integer>, Integer> param) ->
                new ReadOnlyObjectWrapper<>(param.getValue().get(3)));
    }

    /**
     * Function that runs when the algorithm starts
     */
    private void start() {
        // Start the timerTimer
        startTimer();
        // Initialize the polling timer
        pollTimer = new Timer();
        // Initialize bar chart
        initBarChart();
        // Initialize CPU and Memory Charts
        initLoadCharts();
        // Start the algorithm
        thread.start();
        // Initialize the poll timer task
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Get information
                int iBranches = thread.getBranches();
                int iBound = thread.getUpperbound();
                int iValidSchedules = thread.getValidSchedules();
                int iPruned = thread.getPruned();
                int[][] schedule = thread.getSchedule();
                // Convert schedule into a list that we use to display the information
                ArrayList<List<Integer>> scheduleList = new ArrayList<>();
                if (schedule != null) {
                    for (int i = 0; i < schedule.length; i++) {
                        if (schedule.length != 0 && schedule[0] != null) {
                            scheduleList.add(new ArrayList<>());
                            for (int j = 0; j < schedule[0].length; j++) {
                                scheduleList.get(i).add(schedule[i][j]);
                            }
                        }
                    }
                    scheduleList.forEach(intList -> intList.add(scheduleList.indexOf(intList)));
                }
                boolean done = thread.isDone();

                // Update visual information
                Platform.runLater(() -> {
                    branches.setText(iBranches + "");
                    bound.setText(iBound + "");
                    validSchedules.setText(iValidSchedules + "");
                    pruned.setText(iPruned + "");
                    scheduleTable.getItems().setAll(scheduleList);
                    updateBarChart(schedule);
                    if (done) {
                        finish();
                    }
                });
            }
        }, 0, POLL_INTERVAL);

        // Update button state
        start.setDisable(true);
        stop.setDisable(false);
    }

    /**
     * Function that runs when the algorithm has been prematurely terminated
     */
    private void stop() {
        // Stop the algorithm
        thread.interrupt();

        // Stop the timers
        timerTimer.cancel();
        pollTimer.cancel();
        loadTimers.forEach(timer -> timer.cancel());

        // Show that the algorithm has been manually stopped
        topBar.setStyle(TOP_BAR_STOPPED);

        // Update button state
        stop.setDisable(true);
    }

    private void write() {
        scheduler.writeOutput(thread.getFinishedSchedule());
        write.setDisable(true);
        showAlert(WRITE_SUCCESS_ICON, WRITE_SUCCESS_DIALOG_TITLE, WRITE_SUCCESS_DIALOG_BODY);
    }

    private void showAlert(String image, String title, String body) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource(APPLICATION_ICON_PATH).toString()));
        ImageView imageView = new ImageView(this.getClass().getResource(image).toString());
        imageView.setFitHeight(DIALOG_ICON_SIZE);
        imageView.setFitWidth(DIALOG_ICON_SIZE);
        alert.getDialogPane().setPadding(new Insets(DIALOG_PADDING));
        alert.setGraphic(imageView);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(body);
        alert.show();
    }

    /**
     * Function that runs when the algorithm has successfully completed
     */
    private void finish() {
        // Make sure this method will only run once
        if (finished == true) {
            return;
        }
        finished = true;

        // Cancel the timers
        timerTimer.cancel();
        pollTimer.cancel();
        loadTimers.forEach(timer -> timer.cancel());

        // Indicate algorithm successfully completed
        topBar.setStyle(TOP_BAR_SUCCESS);

        // Update button state
        stop.setDisable(true);
        write.setDisable(false);

        showAlert(ALGORITHM_SUCCESS_ICON, ALGORITHM_SUCCESS_DIALOG_TITLE, ALGORITHM_SUCCESS_DIALOG_BODY);
    }

    /**
     * Function that starts the timer on the GUI
     */
    private void startTimer() {
        // Get startTime
        startTime = System.currentTimeMillis();
        // Initialize timer
        timerTimer = new Timer();
        // Indicate that algorithm is running
        topBar.setStyle(TOP_BAR_RUNNING);
        // Update time
        timerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long durationInMillis = System.currentTimeMillis() - startTime;
                long millis = durationInMillis % 1000;
                long second = (durationInMillis / 1000) % 60;
                long minute = (durationInMillis / (1000 * 60)) % 60;
                long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
                Platform.runLater(() -> time.setText(String.format("%02d:%02d:%02d.%d", hour, minute, second, millis)));
            }
        }, 0, TIMER_UPDATE_INTERVAL);
    }

    /**
     * Function that will initialize a time ticking chart with a value to be with the system
     * @param x xAxis
     * @param y yAxis
     * @param chart
     * @param bean System Bean
     * @param start Start time
     * @param valueFunction Function that returns the value
     */
    private void initChart(
            NumberAxis x,
            NumberAxis y,
            LineChart chart,
            OperatingSystemMXBean bean,
            long start,
            Function<OperatingSystemMXBean, Double> valueFunction

    ) {

        // Create data
        ObservableList<XYChart.Series<Number, Number>> data =
                FXCollections.observableArrayList(
                        new LineChart.Series(FXCollections.observableArrayList())
                );

        // Set view options and set data
        x.setAutoRanging(false);
        y.setAutoRanging(false);
        y.setLowerBound(0);
        y.setUpperBound(100);
        x.setOpacity(0);
        chart.setData(data);
        y.setTickUnit(10);
        x.setTickUnit(1000);
        chart.setCreateSymbols(false);

        chart.setAnimated(false);

        // Set up ticking
        Timer timer = new Timer();
        loadTimers.add(timer);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Get value and time
                double value = valueFunction.apply(bean);
                double time = (System.currentTimeMillis() - start);

                // Update the view
                Platform.runLater(() -> {

                    data.get(0).getData().addAll(new XYChart.Data<>(time, value));
                    x.setUpperBound(time);
                });
            }
        };

        // Schedule ticking at 1 sec
        timer.scheduleAtFixedRate(timerTask, 0, LOAD_UPDATE_INTERVAL);
    }

    /**
     * Initializes the CPU and Memory charts
     */
    private void initLoadCharts() {
        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long start = System.currentTimeMillis();

        initChart(cpuX, cpuY, cpuChart, bean, start, this::getCpuUsage);
        initChart( memoryX, memoryY, memoryChart, bean, start, this::getMemoryUsage);

    }

    /**
     * Returns current system CPU usage
     * @param bean
     * @return
     */
    private double getCpuUsage(OperatingSystemMXBean bean) {
        return bean.getSystemCpuLoad() * 100;
    }

    /**
     * Returns current system memory usage by this application
     * @param bean
     * @return
     */
    private double getMemoryUsage(OperatingSystemMXBean bean) {
        return (double)bean.getCommittedVirtualMemorySize() * 100 / (double)bean.getTotalPhysicalMemorySize();
    }

    /**
     * Initializes the bar chart
     */
    private void initBarChart() {
        // One category for each processor
        ArrayList<String> array = new ArrayList<>();
        for (int i = 0; i < thread.getProcessors(); i++) {
            array.add(i + "");
        }
        barX.setCategories(FXCollections.observableArrayList(array));

        barX.setLabel(BAR_X_LABEL);
        barY.setLabel(BAR_Y_LABEL);

        barChart.setAnimated(false);
        barChart.setLegendVisible(false);
    }

    /**
     * Updates the bar chart
     * @param schedule schedule for bar chart for which bar chart is to be modelled
     */
    private void updateBarChart(int[][] schedule) {
        // Reset the data
        barChart.getData().clear();

        // Make sure schedule is not null
        if (schedule == null) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Inner array list is a list of tasks, outer array list indicates processor
        ArrayList<ArrayList<Integer[]>> transformedSchedule = new ArrayList<>();

        // Init array lists (one for each processor)
        for (int i = 0; i < thread.getProcessors(); i++) {
            transformedSchedule.add(new ArrayList<>());
        }

        // Set up processor indexed tasks
        for (int i = 0; i < schedule.length; i++) {
            ArrayList<Integer[]> procList = transformedSchedule.get(schedule[i][2]);
            Integer[] task = new Integer[TASK_LENGTH];
            task[START_TIME] = schedule[i][Schedule.START_TIME];
            task[END_TIME] = schedule[i][Schedule.END_TIME];
            task[NODE_ID] = i;
            task[PROC_TIME] = taskMap.get(i).getProcessTime();
            procList.add(task);
        }

        // Sort schedules based off start time
        for (int i = 0; i < transformedSchedule.size(); i++) {
            transformedSchedule.get(i).sort((t1, t2) -> {
                int startTime1 = t1[Schedule.START_TIME];
                int startTime2 = t2[Schedule.START_TIME];
                if (startTime1 == startTime2) {
                    return 0;
                } else if (startTime1 > startTime2) {
                    return 1;
                } else {
                    return -1;
                }
            });
        }

        // Add the idle times (node id = -1)
        for (int i = 0; i < transformedSchedule.size(); i++) {
            ArrayList<Integer[]> proc = transformedSchedule.get(i);
            int j = 1;
            // Since in the loop below we go check (j, j - 1), we need to zeroth case manually
            // Start off by checking if first start time is not 0, thus we need to put idle time in
            if (proc.size() != 0 && proc.get(0)[START_TIME] != 0) {
                Integer[] idle = new Integer[TASK_LENGTH];
                idle[START_TIME] = 0;
                idle[END_TIME] = proc.get(0)[Schedule.START_TIME];
                idle[NODE_ID] = IDLE_NODE_ID;
                idle[PROC_TIME] = proc.get(0)[Schedule.START_TIME];
                proc.add(0, idle);
            }
            while (proc.size() > j) {
                Integer[] idle = new Integer[TASK_LENGTH];
                // Check if we need to put idle time and if we do, then we put some idle time.
                if (proc.get(j)[START_TIME].equals(proc.get(j - 1)[START_TIME])) {
                    idle[START_TIME] = proc.get(j - 1)[Schedule.END_TIME];
                    idle[END_TIME] = proc.get(j)[Schedule.START_TIME];
                    idle[NODE_ID] = IDLE_NODE_ID;
                    idle[PROC_TIME] = proc.get(j)[Schedule.START_TIME] - proc.get(j - 1)[Schedule.END_TIME];
                    proc.add(j, idle);
                    j++;
                }
                j++;
            }
        }

        // Add the data in
        for (int i = 0; i < transformedSchedule.size(); i++) {
            for (int j = 0; j < transformedSchedule.get(i).size(); j++) {
                Integer[] data = transformedSchedule.get(i).get(j);
                final XYChart.Data<String, Number> visualBar = new XYChart.Data<>(i + "", data[PROC_TIME]);
                visualBar.nodeProperty().addListener((ov, oldNode, node) -> {
                    if (node != null) {
                        setNodeStyle(node, data);
                    }
                });
                series.getData().add(visualBar);
            }
        }

        barChart.getData().addAll(series);
    }

    /**
     * Paints a bar chart node to be beautiful :)
     * @param node JFX Node
     * @param data Data to paint the node with
     */
    private void setNodeStyle(Node node, Integer[] data) {
        // Text is the node label
        Text dataText = new Text();
        dataText.setStyle(BAR_LABEL_STYLE);

        // If the node is idle time, make it transparent, else make it green,
        // insets are for a more visible border between the tasks
        if (data[NODE_ID] == IDLE_NODE_ID) {
            node.setStyle(BAR_IDLE);
        } else {
            node.setStyle(BAR_TASK);
            dataText.setText(data[NODE_ID] + "");
        }

        // Listeners will be called when node is added, we add text in
        node.parentProperty().addListener((ov, oldParent, parent) -> {
            Group parentGroup = (Group) parent;
            if (parentGroup == null) {
                return;
            }
            parentGroup.getChildren().add(dataText);
        });
        node.boundsInParentProperty().addListener((ov, oldBounds, bounds) -> {
            dataText.toFront();
            // Put text in the correct place
            dataText.setLayoutX(
                    Math.round(
                            bounds.getMinX() + bounds.getWidth() / 2 - dataText.prefWidth(-1) / 2
                    )
            );
            dataText.setLayoutY(
                    Math.round(
                            bounds.getMinY() + (bounds.getHeight() / 2) + (dataText.prefHeight(-1) / 2) - 4
                    )
            );
        });
    }

}
