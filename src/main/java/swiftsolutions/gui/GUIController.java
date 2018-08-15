package swiftsolutions.gui;

import com.sun.management.OperatingSystemMXBean;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;
import swiftsolutions.Scheduler;
import swiftsolutions.interfaces.taskscheduler.Algorithm;
import swiftsolutions.interfaces.taskscheduler.VisualAlgorithm;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GUIController {
    @FXML
    private Text time;
    @FXML
    private Button stop;
    @FXML
    private Button start;
    @FXML
    private LineChart<Number, Number> memoryChart;
    @FXML
    private NumberAxis memoryX;
    @FXML
    private NumberAxis memoryY;
    @FXML
    private Text branches;
    @FXML
    private Text bound;
    @FXML
    private Text validSchedules;
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


    private long startTime;
    private long baseTime;
    private Timer timerTimer;
    private Timer pollTimer;
    private VisualAlgorithm thread;
    private Scheduler scheduler;



    @FXML
    private void initialize() {
        stop.setDisable(true);
        start.setOnMouseClicked((MouseEvent event) -> start());
        stop.setOnMouseClicked((MouseEvent event) -> stop());
        initMemoryChart();
        baseTime = 0;
        pollTimer = new Timer();
        nodeIdCol.setCellFactory(col -> {
            TableCell<List<Integer>, String> cell = new TableCell<>();
            cell.textProperty().bind(Bindings.when(cell.emptyProperty())
                    .then("")
                    .otherwise(cell.indexProperty().asString()));
            return cell ;
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
                branches.setText(thread.getBranches() + "");
                bound.setText(thread.getUpperbound() + "");
                validSchedules.setText(thread.getValidSchedules() + "");
                int[][] schedule = thread.getSchedule();
                if (schedule != null) {
                    ArrayList<List<Integer>> scheduleList = new ArrayList<>();
                    for (int i = 0; i < schedule.length; i++) {
                        scheduleList.add(new ArrayList<>());
                        if (schedule.length != 0 && schedule[0] != null) {
                            for (int j = 0; j < schedule[0].length; j++) {
                                scheduleList.get(i).add(schedule[i][j]);
                            }
                        }
                    }
                    scheduleTable.getItems().setAll(scheduleList);
                }
            }
        }, 0, 10);
        start.setDisable(true);
        stop.setDisable(false);
    }

    private void stop() {
        baseTime += System.currentTimeMillis() - startTime;
        timerTimer.cancel();
        pollTimer.cancel();
        stop.setDisable(true);
        if (!thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        timerTimer = new Timer();
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
                    if (thread.isDone()) {
                        scheduler.writeOutput(thread.getFinishedSchedule());
                        stop();
                    }
                });
            }
        }, 0, 5);
    }

    private void initMemoryChart() {
        ObservableList<XYChart.Series<Number,Number>> dataChart =
                FXCollections.observableArrayList(
                        new LineChart.Series("Memory",FXCollections.observableArrayList())
                );

        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        memoryX.setAutoRanging(false);
        memoryY.setAutoRanging(false);
        memoryY.setLowerBound(0);
        memoryY.setUpperBound(100);
        memoryX.setOpacity(0);
        memoryChart.setData(dataChart);
        memoryY.setTickUnit(10);

        long start = System.currentTimeMillis();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (dataChart.get(0).getData().size() > 5) {
                        dataChart.get(0).getData().remove(0);
                        memoryX.setLowerBound(dataChart.get(0).getData().get(0).getXValue().doubleValue());
                    }
                    double value = operatingSystemMXBean.getSystemCpuLoad() * 100;
                    double time = (System.currentTimeMillis() - start)/1000;
                    dataChart.get(0).getData().add(new XYChart.Data<>(time, value));
                    memoryX.setUpperBound(time);

                });
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0,1000);

    }

    public void setAlgorithmThread(VisualAlgorithm thread) {
        this.thread = thread;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


}
