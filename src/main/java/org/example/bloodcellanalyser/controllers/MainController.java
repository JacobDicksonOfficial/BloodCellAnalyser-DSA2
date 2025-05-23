package org.example.bloodcellanalyser.controllers; // Defines the package this class belongs to

// JavaFX imports for UI functionality
import javafx.collections.FXCollections; // For creating observable lists
import javafx.collections.ObservableList; // Observable list type for reactive UI updates
import javafx.fxml.FXML; // Annotation to link UI components from FXML
import javafx.fxml.Initializable; // Interface for initializing controller
import javafx.scene.canvas.Canvas; // A drawing surface for custom graphics
import javafx.scene.canvas.GraphicsContext; // Tool to draw on a Canvas
import javafx.scene.control.*; // Includes UI controls like buttons, tables, sliders, etc.
import javafx.scene.image.*; // Provides classes for image handling
import javafx.scene.paint.Color; // Represents color values
import javafx.scene.text.Font; // Represents font styling
import javafx.scene.text.Text; // A text node in the UI
import javafx.stage.FileChooser; // Dialog for choosing files
import javafx.stage.Stage; // Represents a JavaFX application window
import javafx.scene.control.Tooltip; // Tooltip control for hover text

// Project-specific imports
import org.example.bloodcellanalyser.MyUnionFind; // Custom Union-Find class for pixel grouping
import org.example.bloodcellanalyser.models.BloodCellDetails; // Model holding blood cell data
import org.example.bloodcellanalyser.models.CellSummary; // Model used in summary table view

// Java standard imports
import java.io.File; // For file object
import java.net.URL; // For loading resource URLs
import java.util.*; // For collections (Map, List, etc.)

public class MainController implements Initializable { // Controller for the JavaFX UI

    public static MainController mainController; // Static reference for external access
    private FileChooser fileChooser = new FileChooser(); // Used to open image files

    // UI component bindings from FXML
    @FXML private ImageView originalView, tricolorView, resultView; // Shows original, tricolor, and result images
    @FXML private Slider hueS, satS, brightS; // Sliders for hue, saturation, and brightness
    @FXML private TextField hueValue, satValue, brightValue; // Displays numerical slider values
    @FXML private ListView<String> infoList; // Displays result statistics
    @FXML private ToggleButton toggleLabels; // Toggles showing of cell labels
    @FXML private ProgressIndicator progressIndicator; // Optional loading spinner
    @FXML private Text dropImageHint; // Hint text shown when no image is loaded

    // Table to show cell type, count, and percentage
    @FXML private TableView<CellSummary> resultsTable;
    @FXML private TableColumn<CellSummary, String> cellTypeColumn;
    @FXML private TableColumn<CellSummary, Integer> countColumn;
    @FXML private TableColumn<CellSummary, String> percentageColumn;

    // Variables used for processing and tracking state
    private Image originalImage; // Stores loaded image
    private PixelReader pixelReader; // Reads individual pixel values
    private int width, height; // Dimensions of the image
    private int[] cellArray; // Stores classified pixels: -1 = red, -2 = white, 0 = background
    private MyUnionFind uf; // Union-Find structure for finding pixel groups
    private HashMap<Integer, BloodCellDetails> cells = new HashMap<>(); // Maps cluster roots to blood cell objects
    private boolean lastAnalyzed = false; // Prevents re-analysis without new image

    private double avgRedSize = 70.0; // Starting average size estimate for a red cell

    // Setter for width
    public void setWidth(int width) { this.width = width; }

    // Setter for height
    public void setHeight(int height) { this.height = height; }

    // Setter for image pixel classification array
    public void setImageArray(int[] cellArray) { this.cellArray = cellArray; }

    // Setter for cells hashmap
    public void setPills(HashMap<Integer, BloodCellDetails> map) { this.cells = map; }

    // Called automatically after FXML is loaded
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainController = this; // Store static reference
        // Link table columns to CellSummary model properties
        cellTypeColumn.setCellValueFactory(data -> data.getValue().cellTypeProperty());
        countColumn.setCellValueFactory(data -> data.getValue().countProperty().asObject());
        percentageColumn.setCellValueFactory(data -> data.getValue().percentageProperty());
    }

    // Opens an image file and initializes variables
    @FXML
    public void openImage() {
        File file = fileChooser.showOpenDialog(new Stage()); // Prompt user to choose a file
        if (file != null) {
            originalImage = new Image(file.toURI().toString()); // Load image from file
            originalView.setImage(originalImage); // Show image
            dropImageHint.setVisible(false); // Hide hint text
            pixelReader = originalImage.getPixelReader(); // Allow pixel-by-pixel access
            width = (int) originalImage.getWidth(); // Store image width
            height = (int) originalImage.getHeight(); // Store image height
            cellArray = new int[width * height]; // Prepare classification array
            uf = new MyUnionFind(width * height); // Initialize Union-Find structure
            infoList.getItems().clear(); // Clear previous results
            resultView.setImage(null); // Reset result image
            tricolorView.setImage(null); // Reset tricolor image
            lastAnalyzed = false; // Flag image as not yet analyzed
        }
    }

    // Converts image to red/white/purple pixel format
    @FXML
    public void convertToTricolor() {
        WritableImage tricolor = new WritableImage(width, height); // Prepare output image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = pixelReader.getColor(x, y); // Read color of current pixel
                double hue = c.getHue(); // Extract hue
                double sat = c.getSaturation(); // Extract saturation
                double bright = c.getBrightness(); // Extract brightness
                int index = y * width + x; // Flatten 2D into 1D index

                // Red blood cell pixel
                if ((hue > 330 || hue < 20) && sat > 0.2 && bright > 0.4) {
                    tricolor.getPixelWriter().setColor(x, y, Color.RED); // Mark as red
                    cellArray[index] = -1;
                }
                // White blood cell pixel
                else if ((hue > 200 && hue < 280) && sat > 0.4 && bright < 0.8) {
                    tricolor.getPixelWriter().setColor(x, y, Color.PURPLE); // Mark as purple
                    cellArray[index] = -2;
                }
                // Background pixel
                else {
                    tricolor.getPixelWriter().setColor(x, y, Color.WHITE); // Mark as background
                    cellArray[index] = 0;
                }
            }
        }
        tricolorView.setImage(tricolor); // Display tricolor result
    }

    // Detects clusters of pixels and draws boxes
    @FXML
    public void analyzeCells() {
        // Group neighboring red/purple pixels using Union-Find
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                if (cellArray[idx] < 0) {
                    if (x + 1 < width && cellArray[idx + 1] == cellArray[idx])
                        uf.union(idx, idx + 1);
                    if (y + 1 < height && cellArray[idx + width] == cellArray[idx])
                        uf.union(idx, idx + width);
                }
            }
        }

        // Map each cluster root to a list of pixels
        Map<Integer, List<Integer>> clusters = new HashMap<>();
        for (int i = 0; i < cellArray.length; i++) {
            if (cellArray[i] < 0) {
                int root = uf.find(i);
                clusters.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
            }
        }

        WritableImage result = new WritableImage(width, height); // Prepare final image
        Canvas canvas = new Canvas(width, height); // Create drawing surface
        GraphicsContext gc = canvas.getGraphicsContext2D(); // Get graphics context
        gc.drawImage(originalImage, 0, 0); // Draw original image onto canvas

        int redCount = 0, whiteCount = 0, label = 1; // Initialize counts
        int redTotalSize = 0, redCellCount = 0; // Track red cell sizes
        cells.clear(); // Clear previous cell data

        // Process each cluster
        for (Map.Entry<Integer, List<Integer>> entry : clusters.entrySet()) {
            List<Integer> indices = entry.getValue(); // Get all pixels in cluster
            int size = indices.size(); // Number of pixels
            if (size < 20) continue; // Skip small noise

            int minX = width, maxX = 0, minY = height, maxY = 0; // Bounding box
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

            // Classify cluster
            if (cellArray[indices.get(0)] == -2) {
                rectColor = Color.PURPLE;
                whiteCount++;
                type = "white";
            } else if (size < 60) {
                rectColor = Color.GREEN;
                redCount++;
                type = "red";
                redTotalSize += size;
                redCellCount++;
            } else {
                rectColor = Color.BLUE;
                type = "red-cluster";
            }

            // Store cell info
            BloodCellDetails cell = new BloodCellDetails(type, label++, size, entry.getKey(), rectColor);
            cell.getIndices().addAll(indices);
            cell.setBounds(minX, minY, maxX, maxY);
            cells.put(entry.getKey(), cell);

            // Estimate red cells in a cluster
            if (type.equals("red-cluster")) {
                double avgSize = redCellCount > 0 ? (double) redTotalSize / redCellCount : 70.0;
                redCount += Math.max(2, (int) Math.round(size / avgSize));
            }

            // Draw bounding boxes
            gc.setStroke(rectColor);
            gc.setLineWidth(2);
            gc.strokeRect(minX, minY, maxX - minX + 1, maxY - minY + 1);

            // Draw label if toggled on
            if (toggleLabels.isSelected()) {
                gc.setFont(Font.font(16));
                gc.setFill(Color.BLACK);
                gc.fillText(String.valueOf(cell.getLabel()), minX, minY);
            }
        }

        avgRedSize = redCellCount > 0 ? (double) redTotalSize / redCellCount : 70.0; // Update average size

        canvas.snapshot(null, result); // Take snapshot of canvas
        resultView.setImage(result); // Show result
        lastAnalyzed = true; // Mark as analyzed

        // Update list view
        infoList.getItems().clear();
        infoList.getItems().add("Estimated Red Cells: " + redCount);
        infoList.getItems().add("Estimated White Cells: " + whiteCount);
        infoList.getItems().add("Total Clusters: " + cells.size());

        // Update summary table
        ObservableList<CellSummary> summaryList = FXCollections.observableArrayList();
        int total = redCount + whiteCount;
        if (total > 0) {
            summaryList.add(new CellSummary("Red Cells", redCount, String.format("%.1f%%", 100.0 * redCount / total)));
            summaryList.add(new CellSummary("White Cells", whiteCount, String.format("%.1f%%", 100.0 * whiteCount / total)));
        } else {
            summaryList.add(new CellSummary("Red Cells", 0, "0%"));
            summaryList.add(new CellSummary("White Cells", 0, "0%"));
        }
        resultsTable.setItems(summaryList);

        // Tooltip on mouse hover
        resultView.setOnMouseMoved(event -> {
            int x = (int) event.getX();
            int y = (int) event.getY();

            for (BloodCellDetails cell : cells.values()) {
                if (cell.getType().contains("red") && cell.contains(x, y)) {
                    int estCount = cell.getType().equals("red") ? 1 : Math.max(2, (int) Math.round(cell.getSize() / avgRedSize));
                    Tooltip tip = new Tooltip("Estimated blood cells: " + estCount);
                    Tooltip.install(resultView, tip);
                    return;
                }
            }
            Tooltip.uninstall(resultView, null);
        });
    }

    // Redraws image when toggling labels on/off
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

    // Resets everything in the UI to its initial state
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

    // Getter for original image (used elsewhere)
    public Image getOriginalImage() {
        return originalImage;
    }
}
