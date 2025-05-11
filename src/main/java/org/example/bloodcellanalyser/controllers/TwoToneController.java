package org.example.bloodcellanalyser.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import org.example.bloodcellanalyser.HelloApplication;

import java.net.URL;
import java.util.ResourceBundle;

public class TwoToneController implements Initializable {

    public static TwoToneController twoToneController;

    @FXML private ImageView imageView;

    public ImageView getImageView() {
        return imageView;
    }

    @FXML
    public void backToMain() {
        HelloApplication.mainStage.setScene(HelloApplication.mainS);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        twoToneController = this;
    }
}