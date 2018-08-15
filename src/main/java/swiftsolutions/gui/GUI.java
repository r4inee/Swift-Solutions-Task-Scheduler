package swiftsolutions.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import swiftsolutions.Scheduler;
import swiftsolutions.interfaces.taskscheduler.ParallelAlgorithm;
import swiftsolutions.interfaces.taskscheduler.VisualAlgorithm;
import swiftsolutions.taskscheduler.Schedule;
import swiftsolutions.taskscheduler.Task;

import java.util.Map;


public class GUI extends Application {

    private GUIController _guiController;

    public void setAlgorithmThread(VisualAlgorithm thread) {
        _guiController.setAlgorithmThread(thread);
    }

    public void setScheduler(Scheduler scheduler) {
        _guiController.setScheduler(scheduler);
    }

    public void setTaskMap(Map<Integer, Task> taskMap) { _guiController.setTaskMap(taskMap); }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("template/GUI.fxml"));
            Parent root = loader.load();
            _guiController = loader.getController();
            Scene scene = new Scene(root, 1200, 800);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNIFIED);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("template/icon.png")));
            stage.setResizable(false);
            stage.setOnCloseRequest(event -> System.exit(0));
            stage.setTitle(primaryStage.getTitle());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
