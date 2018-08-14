package swiftsolutions.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import swiftsolutions.interfaces.taskscheduler.ParallelAlgorithm;
import swiftsolutions.interfaces.taskscheduler.VisualAlgorithm;


public class GUI extends Application {

    private GUIController _guiController;

    public void setAlgorithm(VisualAlgorithm algorithm) {
        _guiController.setAlgorithm(algorithm);
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
