package org.example.bloodcellanalyser; // Declares the package this class belongs to

// JavaFX imports needed for GUI setup
import javafx.application.Application; // Base class for all JavaFX applications
import javafx.fxml.FXMLLoader; // Used to load FXML files into JavaFX scenes
import javafx.scene.Scene; // Represents a JavaFX scene (the container for UI)
import javafx.stage.Stage; // Represents the main window (stage) of the JavaFX application

// Main application class that launches the Blood Cell Analyser GUI
public class HelloApplication extends Application {

    // Static references to all scenes used in the application for easy scene switching
    public static Scene mainS, viewS, twoToneS;

    // Static reference to the main stage (window) of the application
    public static Stage mainStage;

    // Utility method to load an FXML file and wrap it in a Scene object with a fixed size
    public static Scene loadScene(String file) throws Exception {
        // Creates a new FXMLLoader using the specified FXML file (from resources folder)
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(file));

        // Loads the FXML and wraps the root node in a new Scene (1400x850 size)
        return new Scene(loader.load(), 1400, 850);
    }

    // Entry point for the JavaFX application — called automatically when app starts
    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage; // Save the reference to the main stage for static access

        // Load all scenes (from FXML files) used in the application
        mainS = loadScene("hello-view.fxml");        // Main screen
        viewS = loadScene("view-view.fxml");         // Preview-only screen
        twoToneS = loadScene("two-tone-view.fxml");  // Two-tone image screen

        // Set the window title shown on the title bar
        stage.setTitle("Blood Cell Analyser");

        // Set the default scene to the main scene
        stage.setScene(mainS);

        // Make the window visible
        stage.show();
    }

    // Main method — launches the JavaFX application
    public static void main(String[] args) {
        launch(); // Calls the start() method automatically
    }
}
