package swiftsolutions.gui;

import com.sun.management.OperatingSystemMXBean;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import swiftsolutions.interfaces.output.OutputManager;
import swiftsolutions.output.VisualizationMessage;
import swiftsolutions.output.VisualizationMessageType;
import swiftsolutions.util.Observable;
import swiftsolutions.util.Observer;

import java.lang.management.ManagementFactory;
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

    private long startTime;
    private long baseTime;
    private Timer timerTimer;
    private OutputManager outputManager;


    @FXML
    private void initialize() {
        stop.setDisable(true);
        start.setOnMouseClicked((MouseEvent event) -> start());
        stop.setOnMouseClicked((MouseEvent event) -> stop());
        initMemoryChart();
        baseTime = 0;
    }

    private void start() {
        startTimer();

        start.setDisable(true);
        stop.setDisable(false);
    }

    private void stop() {
        baseTime += System.currentTimeMillis() - startTime;
        timerTimer.cancel();
        start.setDisable(false);
        stop.setDisable(true);
    }

    private void startTimer() {
        outputManager.sendVisual(new VisualizationMessage("", VisualizationMessageType.START));
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
                Platform.runLater(() -> time.setText(String.format("%02d:%02d:%02d.%d", hour, minute, second, millis)));
            }
        }, 0, 1);
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
                    double memory = (double)operatingSystemMXBean.getFreePhysicalMemorySize()/(double)operatingSystemMXBean.getTotalPhysicalMemorySize();
                    double value = memory * 100;
                    double time = (System.currentTimeMillis() - start)/1000;
                    dataChart.get(0).getData().add(new XYChart.Data<>(time, value));
                    memoryX.setUpperBound(time);

                });
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0,1000);

    }

    public void setOutputManager(OutputManager outputManager) {
        this.outputManager = outputManager;
        this.outputManager.addVisualObserver(new Observer<VisualizationMessage>() {
            @Override
            public void update(Observable<? extends VisualizationMessage> observer, VisualizationMessage arg) {
                switch (arg.getType()) {
                    case BRANCH_AMOUNT:
                        Platform.runLater(() -> branches.setText((Integer.parseInt(branches.getText()) + 1) + ""));
                        break;
                }
            }
        });


        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                outputManager.notifyVisual();
            }
        }, 0, 1000);
    }
}
