package org.example.bloodcellanalyser.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import org.example.bloodcellanalyser.HelloApplication;

import java.net.URL;
import java.util.ResourceBundle;

public class ViewController implements Initializable {

    public static ViewController viewController;

    @FXML public ImageView imagePreview;
    @FXML public Button backBtn;

    @FXML
    public void backToMain() {
        HelloApplication.mainStage.setScene(HelloApplication.mainS);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        viewController = this;
    }
}
