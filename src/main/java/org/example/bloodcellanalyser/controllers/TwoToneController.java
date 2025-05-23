package org.example.bloodcellanalyser.controllers; // Declares this class is part of the 'controllers' package

// JavaFX imports for UI components and lifecycle handling
import javafx.fxml.FXML; // Allows JavaFX to inject UI components from FXML files
import javafx.fxml.Initializable; // Interface used for initializing controller classes after FXML is loaded
import javafx.scene.image.ImageView; // ImageView is used to display images in JavaFX

// Importing application class that holds static references to scenes and the main stage
import org.example.bloodcellanalyser.HelloApplication; // Used here to switch back to the main scene

import java.net.URL; // Used for locating resources
import java.util.ResourceBundle; // Used for localization, passed during initialization

// Controller class for the TwoTone view/window
public class TwoToneController implements Initializable {

    // Static reference to this controller instance, similar to a singleton pattern
    public static TwoToneController twoToneController;

    // This ImageView is linked from the FXML file and will be used to display an image in the UI
    @FXML private ImageView imageView;

    // Public getter to allow other classes to access the ImageView
    public ImageView getImageView() {
        return imageView;
    }

    // Method triggered when a UI element (like a button) calls backToMain in FXML
    // It changes the scene displayed on the main application stage to the main scene
    @FXML
    public void backToMain() {
        HelloApplication.mainStage.setScene(HelloApplication.mainS); // Switch scene to the main scene
    }

    // Called automatically after the FXML file has been loaded
    // Useful for initializing state or setting up data bindings
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        twoToneController = this; // Set the static reference to this instance so it can be accessed globally
    }
}
