package swiftsolutions.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import swiftsolutions.Scheduler;
import swiftsolutions.interfaces.taskscheduler.ParallelAlgorithm;
import swiftsolutions.interfaces.taskscheduler.VisualAlgorithm;
import swiftsolutions.taskscheduler.Schedule;


public class GUI extends Application {

    private GUIController _guiController;

    public void setAlgorithmThread(VisualAlgorithm thread) {
        _guiController.setAlgorithmThread(thread);
    }

    public void setScheduler(Scheduler scheduler) {
        _guiController.setScheduler(scheduler);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("template/GUI.fxml"));
            Parent root = loader.load();
            _guiController = loader.getController();
            Scene scene = new Scene(root, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
