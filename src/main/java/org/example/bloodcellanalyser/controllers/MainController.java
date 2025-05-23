package org.example.bloodcellanalyser.controllers; // Defines the package location of the controller class

// Importing JavaFX and standard Java classes
import javafx.collections.FXCollections; // Used to create observable lists for JavaFX UI components
import javafx.collections.ObservableList; // List that automatically updates UI elements when modified
import javafx.fxml.FXML; // Annotation to link FXML UI components to the controller
import javafx.fxml.Initializable; // Interface for controller classes that need initialization logic
import javafx.scene.canvas.Canvas; // A canvas node for drawing graphics manually
import javafx.scene.canvas.GraphicsContext; // The drawing context for the canvas
import javafx.scene.control.*; // Imports all common JavaFX controls like Button, TableView, etc.
import javafx.scene.image.*; // Handles image display and manipulation
import javafx.scene.paint.Color; // Used for color manipulation and drawing
import javafx.scene.text.Font; // Used to define font style and size
import javafx.scene.text.Text; // Used to manipulate text node on the scene
import javafx.stage.FileChooser; // Allows user to open file dialog
import javafx.stage.Stage; // JavaFX window representation
import org.example.bloodcellanalyser.MyUnionFind; // Custom union-find data structure for cell clustering
import org.example.bloodcellanalyser.models.BloodCellDetails; // Model class that stores blood cell info
import org.example.bloodcellanalyser.models.CellSummary; // Model class used to display cell summary in table

import java.io.File; // Represents a file/directory path
import java.net.URL; // Represents a resource location, used in initialization
import java.util.*; // Imports collections: Map, List, HashMap, etc.

public class MainController implements Initializable { // Controller class for the UI and logic
    public static MainController mainController; // Static reference to controller instance (singleton-like)

    private FileChooser fileChooser = new FileChooser(); // FileChooser instance to pick an image file

    // FXML bindings to UI components defined in the FXML layout
    @FXML private ImageView originalView, tricolorView, resultView; // ImageViews for displaying various images
    @FXML private Slider hueS, satS, brightS; // Sliders for adjusting hue, saturation, brightness
    @FXML private TextField hueValue, satValue, brightValue; // Text fields for current slider values
    @FXML private ListView<String> infoList; // ListView to display textual info like cell counts
    @FXML private ToggleButton toggleLabels; // Toggle button to show/hide cell labels
    @FXML private ProgressIndicator progressIndicator; // Optional loading indicator (currently unused)
    @FXML private Text dropImageHint; // Hint message when no image is loaded

    @FXML private TableView<CellSummary> resultsTable; // Table to show summary of analyzed cells
    @FXML private TableColumn<CellSummary, String> cellTypeColumn; // Table column for cell type
    @FXML private TableColumn<CellSummary, Integer> countColumn; // Table column for count of cells
    @FXML private TableColumn<CellSummary, String> percentageColumn; // Table column for percentage of cells

    // Internal state variables for image analysis
    private Image originalImage; // Holds the original loaded image
    private PixelReader pixelReader; // Allows reading color data from the image pixels
    private int width, height; // Image dimensions
    private int[] cellArray; // Flat array storing classified pixel types (-1 = red, -2 = white, 0 = background)
    private MyUnionFind uf; // Union-find structure to group connected pixels
    private HashMap<Integer, BloodCellDetails> cells = new HashMap<>(); // Map of root ID to blood cell metadata
    private boolean lastAnalyzed = false; // Flag to track if analysis has been performed

    // Setter methods used by other classes or testing
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setImageArray(int[] cellArray) { this.cellArray = cellArray; }
    public void setPills(HashMap<Integer, BloodCellDetails> map) { this.cells = map; }

    // Initializes controller when loaded from FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainController = this; // Save controller reference to static field

        // Bind table columns to properties of CellSummary model
        cellTypeColumn.setCellValueFactory(data -> data.getValue().cellTypeProperty());
        countColumn.setCellValueFactory(data -> data.getValue().countProperty().asObject());
        percentageColumn.setCellValueFactory(data -> data.getValue().percentageProperty());
    }

    // Triggered when "Open Image" is clicked – loads an image from the file system
    @FXML
    public void openImage() {
        File file = fileChooser.showOpenDialog(new Stage()); // Opens dialog to choose image
        if (file != null) {
            originalImage = new Image(file.toURI().toString()); // Load image into JavaFX
            originalView.setImage(originalImage); // Display image in UI
            dropImageHint.setVisible(false); // Hide "Drop Image" hint
            pixelReader = originalImage.getPixelReader(); // Read pixel data from image
            width = (int) originalImage.getWidth(); // Set image width
            height = (int) originalImage.getHeight(); // Set image height
            cellArray = new int[width * height]; // Allocate array for classifying pixels
            uf = new MyUnionFind(width * height); // Initialize Union-Find with pixel count
            infoList.getItems().clear(); // Clear info display
            resultView.setImage(null); // Reset result view
            tricolorView.setImage(null); // Reset tricolor view
            lastAnalyzed = false; // Reset analysis flag
        }
    }

    // Converts the original image to a simplified color-coded format
    @FXML
    public void convertToTricolor() {
        WritableImage tricolor = new WritableImage(width, height); // Create blank image

        // Iterate through each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = pixelReader.getColor(x, y); // Get original pixel color
                double hue = c.getHue(); // Extract hue
                double sat = c.getSaturation(); // Extract saturation
                double bright = c.getBrightness(); // Extract brightness
                int index = y * width + x; // 1D index for cellArray

                // Conditions to classify red cells
                if ((hue > 330 || hue < 20) && sat > 0.2 && bright > 0.4) {
                    tricolor.getPixelWriter().setColor(x, y, Color.RED); // Color the pixel red
                    cellArray[index] = -1; // Store classification
                }
                // Conditions to classify white cells
                else if ((hue > 200 && hue < 280) && sat > 0.4 && bright < 0.8) {
                    tricolor.getPixelWriter().setColor(x, y, Color.PURPLE); // Color the pixel purple
                    cellArray[index] = -2; // Store classification
                }
                // All other pixels are considered background
                else {
                    tricolor.getPixelWriter().setColor(x, y, Color.WHITE); // Color the pixel white
                    cellArray[index] = 0; // Mark as background
                }
            }
        }

        tricolorView.setImage(tricolor); // Display the tricolor image
    }

    // Performs clustering analysis on classified pixels
    @FXML
    public void analyzeCells() {
        // Union neighboring same-type pixels horizontally and vertically
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                if (cellArray[idx] < 0) {
                    if (x + 1 < width && cellArray[idx + 1] == cellArray[idx])
                        uf.union(idx, idx + 1); // Union right neighbor
                    if (y + 1 < height && cellArray[idx + width] == cellArray[idx])
                        uf.union(idx, idx + width); // Union bottom neighbor
                }
            }
        }

        // Group pixels by their Union-Find root
        Map<Integer, List<Integer>> clusters = new HashMap<>();
        for (int i = 0; i < cellArray.length; i++) {
            if (cellArray[i] < 0) {
                int root = uf.find(i); // Find root
                clusters.computeIfAbsent(root, k -> new ArrayList<>()).add(i); // Group by root
            }
        }

        WritableImage result = new WritableImage(width, height); // Final output image
        Canvas canvas = new Canvas(width, height); // Canvas to draw bounding boxes
        GraphicsContext gc = canvas.getGraphicsContext2D(); // Drawing context
        gc.drawImage(originalImage, 0, 0); // Draw original image as base

        int redCount = 0, whiteCount = 0, label = 1; // Counters and label index
        cells.clear(); // Reset previous data

        // Iterate over each cluster to draw bounding boxes and count cells
        for (Map.Entry<Integer, List<Integer>> entry : clusters.entrySet()) {
            List<Integer> indices = entry.getValue();
            int size = indices.size(); // Cluster size
            if (size < 20) continue; // Ignore noise

            int minX = width, maxX = 0, minY = height, maxY = 0; // Bounding box coordinates
            for (int idx : indices) {
                int x = idx % width;
                int y = idx / width;
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }

            Color rectColor;
            String type;

            // Determine cell type and color
            if (cellArray[indices.get(0)] == -2) {
                rectColor = Color.PURPLE;
                whiteCount++;
                type = "white";
            } else if (size < 60) {
                rectColor = Color.GREEN;
                redCount++;
                type = "red";
            } else {
                rectColor = Color.BLUE;
                redCount += Math.round(size / 50.0);
                type = "red-cluster";
            }

            // Create and store BloodCellDetails object
            BloodCellDetails cell = new BloodCellDetails(type, label++, size, entry.getKey(), rectColor);
            cell.getIndices().addAll(indices);
            cells.put(entry.getKey(), cell);

            // Draw bounding rectangle
            gc.setStroke(rectColor);
            gc.setLineWidth(2);
            gc.strokeRect(minX, minY, maxX - minX + 1, maxY - minY + 1);

            // Draw label text
            if (toggleLabels.isSelected()) {
                gc.setFont(Font.font(16));
                gc.setFill(Color.BLACK);
                gc.fillText(String.valueOf(cell.getLabel()), minX, minY);
            }
        }

        canvas.snapshot(null, result); // Convert canvas to image
        resultView.setImage(result); // Show result image
        lastAnalyzed = true; // Mark as analyzed

        // Update text list with results
        infoList.getItems().clear();
        infoList.getItems().add("Estimated Red Cells: " + redCount);
        infoList.getItems().add("Estimated White Cells: " + whiteCount);
        infoList.getItems().add("Total Clusters: " + cells.size());

        // Update results table
        ObservableList<CellSummary> summaryList = FXCollections.observableArrayList();
        int total = redCount + whiteCount;
        if (total > 0) {
            summaryList.add(new CellSummary("Red Cells", redCount, String.format("%.1f%%", 100.0 * redCount / total)));
            summaryList.add(new CellSummary("White Cells", whiteCount, String.format("%.1f%%", 100.0 * whiteCount / total)));
        } else {
            summaryList.add(new CellSummary("Red Cells", 0, "0%"));
            summaryList.add(new CellSummary("White Cells", 0, "0%"));
        }
        resultsTable.setItems(summaryList); // Display in table
    }

    // Re-renders bounding boxes with or without labels
    @FXML
    public void toggleLabelsAction() {
        if (!lastAnalyzed || cells.isEmpty()) return;

        WritableImage result = new WritableImage(width, height);
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(originalImage, 0, 0);

        for (BloodCellDetails cell : cells.values()) {
            int minX = width, maxX = 0, minY = height, maxY = 0;
            for (int idx : cell.getIndices()) {
                int x = idx % width;
                int y = idx / width;
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }

            gc.setStroke(cell.getColor());
            gc.setLineWidth(2);
            gc.strokeRect(minX, minY, maxX - minX + 1, maxY - minY + 1);

            if (toggleLabels.isSelected()) {
                gc.setFont(Font.font(16));
                gc.setFill(Color.BLACK);
                gc.fillText(String.valueOf(cell.getLabel()), minX, minY);
            }
        }

        canvas.snapshot(null, result);
        resultView.setImage(result);
    }

    // Resets all UI elements and values to initial state
    @FXML
    public void handleResetAll() {
        originalView.setImage(null);
        tricolorView.setImage(null);
        resultView.setImage(null);
        infoList.getItems().clear();
        resultsTable.getItems().clear();
        dropImageHint.setVisible(true);

        hueS.setValue(20);
        hueValue.setText("20");

        satS.setValue(0.2);
        satValue.setText("0.2");

        brightS.setValue(0.4);
        brightValue.setText("0.4");

        cells.clear();
    }

    // Getter for original image, possibly used in other components
    public Image getOriginalImage() {
        return originalImage;
    }
}
