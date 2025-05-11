package org.example.bloodcellanalyser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    public static Scene mainS, viewS, twoToneS;
    public static Stage mainStage;

    public static Scene loadScene(String file) throws Exception {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(file));
        return new Scene(loader.load(), 1000, 700);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        mainS = loadScene("hello-view.fxml");
        viewS = loadScene("view-view.fxml");
        twoToneS = loadScene("two-tone-view.fxml");

        stage.setTitle("Blood Cell Analyser");
        stage.setScene(mainS);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
