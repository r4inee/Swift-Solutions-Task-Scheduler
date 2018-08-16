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

/**
 * Interface between the GUI and the Scheduler
 */
public class GUI extends Application {

    private GUIController _guiController;

    /**
     * Sets the algorithm that the GUI will run.
     * @param thread Algorithm
     */
    public void setAlgorithmThread(VisualAlgorithm thread) {
        _guiController.setAlgorithmThread(thread);
    }

    /**
     * Sets the scheduler for writing purposes.
     * @param scheduler
     */
    public void setScheduler(Scheduler scheduler) {
        _guiController.setScheduler(scheduler);
    }

    /**
     * Sets task map so GUI can obtain information about the tasks
     * @param taskMap
     */
    public void setTaskMap(Map<Integer, Task> taskMap) { _guiController.setTaskMap(taskMap); }

    /**
     * Loads the GUI
     * @param primaryStage unused
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("template/GUI.fxml"));
            Parent root = loader.load();

            // Get the controller
            _guiController = loader.getController();

            // Initialize the stage
            Scene scene = new Scene(root, 1200, 800);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNIFIED);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("template/icon.png")));
            stage.setResizable(false);
            stage.setOnCloseRequest(event -> System.exit(0));
            stage.setTitle(primaryStage.getTitle());
            stage.setScene(scene);

            //Start the application
            stage.show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
