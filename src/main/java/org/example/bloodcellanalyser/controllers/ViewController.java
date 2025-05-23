package org.example.bloodcellanalyser.controllers; // Defines the package this class belongs to

// Import necessary JavaFX classes for UI interaction and component lifecycle
import javafx.fxml.FXML; // Used to annotate UI components injected from the FXML file
import javafx.fxml.Initializable; // Interface for initializing the controller after FXML load
import javafx.scene.image.ImageView; // UI component to display images
import javafx.scene.control.Button; // UI component for clickable buttons

// Import the main application class to access stage and scenes
import org.example.bloodcellanalyser.HelloApplication; // Contains static references to main stage and scenes

import java.net.URL; // Represents a resource location (used in initialize method)
import java.util.ResourceBundle; // Used for internationalization and passing runtime resources

// Controller for the View window/page
public class ViewController implements Initializable {

    // Static reference to this controller instance (can be accessed globally if needed)
    public static ViewController viewController;

    // FXML-injected fields from the corresponding FXML layout
    @FXML public ImageView imagePreview; // ImageView for showing an image preview
    @FXML public Button backBtn; // Button that navigates back to the main window

    // Method triggered when the back button is clicked
    // It switches the scene back to the main scene
    @FXML
    public void backToMain() {
        HelloApplication.mainStage.setScene(HelloApplication.mainS); // Change the current stage scene to the main one
    }

    // Automatically called by JavaFX after the FXML file has been loaded
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        viewController = this; // Store this instance in a static variable for global access
    }
}
