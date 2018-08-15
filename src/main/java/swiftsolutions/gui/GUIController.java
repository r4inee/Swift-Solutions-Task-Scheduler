package swiftsolutions.gui;

import com.sun.management.OperatingSystemMXBean;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
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
import swiftsolutions.taskscheduler.Task;

import java.lang.management.ManagementFactory;
import java.util.*;

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
    private TableColumn<List<Integer>, String> nodeIdCol;
    @FXML
    private TableColumn<List<Integer>, String> startTimeCol;
    @FXML
    private TableColumn<List<Integer>, String> endTimeCol;
    @FXML
    private TableColumn<List<Integer>, String> processorCol;
    @FXML
    private StackedBarChart<String, Number> barChart;
    @FXML
    private CategoryAxis barX;
    @FXML
    private NumberAxis barY;

    private long startTime;
    private long baseTime;
    private Timer timerTimer;
    private Timer pollTimer;
    private VisualAlgorithm thread;
    private Scheduler scheduler;
    private boolean finished;
    private Map<Integer, Task> taskMap;


    @FXML
    private void initialize() {
        finished = false;
        topBar.setStyle("-fx-background-color: #676767");
        stop.setDisable(true);
        write.setDisable(true);
        start.setOnMouseClicked((MouseEvent event) -> start());
        stop.setOnMouseClicked((MouseEvent event) -> stop());
        write.setOnMouseClicked(event -> write());
        initLoadCharts();
        baseTime = 0;
        pollTimer = new Timer();
        nodeIdCol.setCellFactory(col -> {
            TableCell<List<Integer>, String> cell = new TableCell<>();
            cell.textProperty().bind(Bindings.when(cell.emptyProperty())
                    .then("")
                    .otherwise(cell.indexProperty().asString()));
            return cell;
        });
        startTimeCol.setCellValueFactory((TableColumn.CellDataFeatures<List<Integer>, String> param) ->
                new SimpleStringProperty(param.getValue().get(0) + ""));
        endTimeCol.setCellValueFactory((TableColumn.CellDataFeatures<List<Integer>, String> param) ->
                new SimpleStringProperty(param.getValue().get(1) + ""));
        processorCol.setCellValueFactory((TableColumn.CellDataFeatures<List<Integer>, String> param) ->
                new SimpleStringProperty(param.getValue().get(2) + ""));
    }

    private void start() {
        startTimer();
        thread.start();
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int iBranches = thread.getBranches();
                int iBound = thread.getUpperbound();
                int iValidSchadules = thread.getValidSchedules();
                int iPruned = thread.getPruned();
                int[][] schedule = thread.getSchedule();
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
                }
                boolean done = thread.isDone();
                Platform.runLater(() -> {
                    branches.setText(iBranches + "");
                    bound.setText(iBound + "");
                    validSchedules.setText(iValidSchadules + "");
                    pruned.setText(iPruned + "");
                    scheduleTable.getItems().setAll(scheduleList);
                    updateBarChart(schedule);
                    if (done) {
                        finish();
                    }
                });
            }
        }, 0, 10);
        start.setDisable(true);
        stop.setDisable(false);
    }

    private void stop() {
        thread.interrupt();
        baseTime += System.currentTimeMillis() - startTime;
        timerTimer.cancel();
        pollTimer.cancel();
        topBar.setStyle("-fx-background-color: #e53935");
        stop.setDisable(true);
    }

    private void write() {
        scheduler.writeOutput(thread.getFinishedSchedule());
        write.setDisable(true);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("template/icon.png").toString()));
        ImageView imageView = new ImageView(this.getClass().getResource("template/notepad.png").toString());
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        alert.getDialogPane().setPadding(new Insets(20));
        alert.setGraphic(imageView);
        alert.setTitle("Output Written");
        alert.setHeaderText(null);
        alert.setContentText("File successfully written");
        alert.show();
    }

    private void finish() {
        if (finished == true) {
            return;
        }
        finished = true;
        timerTimer.cancel();
        pollTimer.cancel();
        topBar.setStyle("-fx-background-color: #46bb7c");
        stop.setDisable(true);
        write.setDisable(false);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("template/icon.png").toString()));
        ImageView imageView = new ImageView(this.getClass().getResource("template/checked.png").toString());
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        alert.getDialogPane().setPadding(new Insets(20));
        alert.setGraphic(imageView);
        alert.setTitle("Algorithm finished!");
        alert.setHeaderText(null);
        alert.setContentText("Algorithm successfully executed!");
        alert.show();
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        timerTimer = new Timer();
        topBar.setStyle("-fx-background-color: #FB8C00");
        timerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long durationInMillis = System.currentTimeMillis() - startTime + baseTime;
                long millis = durationInMillis % 1000;
                long second = (durationInMillis / 1000) % 60;
                long minute = (durationInMillis / (1000 * 60)) % 60;
                long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
                Platform.runLater(() -> {
                    time.setText(String.format("%02d:%02d:%02d.%d", hour, minute, second, millis));
                });
            }
        }, 0, 5);
    }

    private void initLoadCharts() {
        ObservableList<XYChart.Series<Number, Number>> memoryData =
                FXCollections.observableArrayList(
                        new LineChart.Series("Memory", FXCollections.observableArrayList())
                );

        ObservableList<XYChart.Series<Number, Number>> cpuData =
                FXCollections.observableArrayList(
                        new LineChart.Series("CPU", FXCollections.observableArrayList())
                );

        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        memoryX.setAutoRanging(false);
        memoryY.setAutoRanging(false);
        memoryY.setLowerBound(0);
        memoryY.setUpperBound(100);
        memoryX.setOpacity(0);
        memoryChart.setData(memoryData);
        memoryY.setTickUnit(10);

        cpuX.setAutoRanging(false);
        cpuY.setAutoRanging(false);
        cpuY.setLowerBound(0);
        cpuY.setUpperBound(100);
        cpuX.setOpacity(0);
        cpuChart.setData(cpuData);
        cpuY.setTickUnit(10);

        long start = System.currentTimeMillis();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (memoryData.get(0).getData().size() > 5) {
                        memoryData.get(0).getData().remove(0);
                        memoryX.setLowerBound(memoryData.get(0).getData().get(0).getXValue().doubleValue());
                    }
                    if (cpuData.get(0).getData().size() > 5) {
                        cpuData.get(0).getData().remove(0);
                        cpuX.setLowerBound(cpuData.get(0).getData().get(0).getXValue().doubleValue());
                    }
                    double cpuValue = operatingSystemMXBean.getSystemCpuLoad() * 100;
                    double memoryValue = (double) operatingSystemMXBean.getCommittedVirtualMemorySize() * 100
                            / (double) operatingSystemMXBean.getTotalPhysicalMemorySize();
                    double time = (System.currentTimeMillis() - start) / 1000;
                    memoryData.get(0).getData().add(new XYChart.Data<>(time, cpuValue));
                    cpuData.get(0).getData().addAll(new XYChart.Data<>(time, memoryValue));
                    cpuX.setUpperBound(time);
                    memoryX.setUpperBound(time);

                });
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);

    }

    private void initBarChart() {
        ArrayList<String> array = new ArrayList<>();
        for (int i = 0; i < thread.getProcessors(); i++) {
            array.add(i + "");
        }
        barX.setCategories(FXCollections.observableArrayList(array));
        barX.setLabel("Processor");
        barY.setLabel("Time");
        barChart.setAnimated(false);
        barChart.setLegendVisible(false);
    }

    private void updateBarChart(int[][] schedule) {

        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ArrayList<ArrayList<Integer[]>> tranformedSchedule = new ArrayList<>();

        for (int i = 0; i < thread.getProcessors(); i++) {
            tranformedSchedule.add(new ArrayList<>());
        }

        if (schedule != null) {
            for (int i = 0; i < schedule.length; i++) {
                ArrayList<Integer[]> procList = tranformedSchedule.get(schedule[i][2]);
                Integer[] task = new Integer[4];
                task[0] = schedule[i][0];
                task[1] = schedule[i][1];
                task[2] = i;
                task[3] = taskMap.get(i).getProcessTime();
                procList.add(task);
            }
        }

        for (int i = 0; i < tranformedSchedule.size(); i++) {
            tranformedSchedule.get(i).sort((t1, t2) -> {
                int startTime1 = t1[0];
                int startTime2 = t2[0];
                if (startTime1 == startTime2) {
                    return 0;
                } else if (startTime1 > startTime2) {
                    return 1;
                } else {
                    return -1;
                }
            });
        }

        for (int i = 0; i < tranformedSchedule.size(); i++) {
            ArrayList<Integer[]> proc = tranformedSchedule.get(i);
            int j = 1;
            if (proc.size() != 0 && proc.get(0)[0] != 0) {
                Integer[] idle = new Integer[4];
                idle[0] = 0;
                idle[1] = proc.get(0)[0];
                idle[2] = -1;
                idle[3] = proc.get(0)[0];
                proc.add(0, idle);
            }
            while (proc.size() > j) {
                Integer[] idle = new Integer[4];
                if (proc.get(j)[0].equals(proc.get(j - 1)[0])) {
                    idle[0] = proc.get(j - 1)[1];
                    idle[1] = proc.get(j)[0];
                    idle[2] = -1;
                    idle[3] = proc.get(j)[0] - proc.get(j - 1)[1];
                    proc.add(j, idle);
                    j++;
                }
                j++;
            }
        }

        for (int i = 0; i < tranformedSchedule.size(); i++) {
            for (int j = 0; j < tranformedSchedule.get(i).size(); j++) {
                Integer[] data = tranformedSchedule.get(i).get(j);
                final XYChart.Data<String, Number> visualBar = new XYChart.Data<>(i + "", data[3]);
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

    private void setNodeStyle(Node node, Integer[] data) {
        Text dataText = new Text();
        dataText.setStyle("-fx-fill: white");
        if (data[2] == -1) {
            node.setStyle(
                    "-fx-bar-fill: transparent"
            );
        } else {
            node.setStyle("-fx-bar-fill: #71dba1; -fx-background-insets: 1, 0, 0, 1");
            dataText.setText(data[2] + "");
        }

        node.parentProperty().addListener((ov, oldParent, parent) -> {
            Group parentGroup = (Group) parent;
            if (parentGroup == null) {
                return;
            }
            parentGroup.getChildren().add(dataText);
        });


        node.boundsInParentProperty().addListener((ov, oldBounds, bounds) -> {
            dataText.toFront();
            dataText.setLayoutX(
                    Math.round(
                            bounds.getMinX() + bounds.getWidth() / 2 - dataText.prefWidth(-1) / 2
                    )
            );
            dataText.setLayoutY(
                    Math.round(
                            bounds.getMinY() + (bounds.getHeight()/2) + (dataText.prefHeight(-1)/2) - 4
                    )
            );
        });
    }

    public void setAlgorithmThread(VisualAlgorithm thread) {
        this.thread = thread;
        initBarChart();
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setTaskMap(Map<Integer, Task> taskMap) {
        this.taskMap = taskMap;
    }


}
