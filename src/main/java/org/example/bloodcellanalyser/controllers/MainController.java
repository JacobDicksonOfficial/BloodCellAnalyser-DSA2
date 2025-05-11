package org.example.bloodcellanalyser.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.bloodcellanalyser.MyUnionFind;
import org.example.bloodcellanalyser.models.BloodCellDetails;
import org.example.bloodcellanalyser.models.CellSummary;

import java.io.File;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    public static MainController mainController;
    private FileChooser fileChooser = new FileChooser();

    @FXML private ImageView originalView, tricolorView, resultView;
    @FXML private Slider hueS, satS, brightS;
    @FXML private TextField hueValue, satValue, brightValue;
    @FXML private ListView<String> infoList;
    @FXML private ToggleButton toggleLabels;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Text dropImageHint;

    @FXML private TableView<CellSummary> resultsTable;
    @FXML private TableColumn<CellSummary, String> cellTypeColumn;
    @FXML private TableColumn<CellSummary, Integer> countColumn;
    @FXML private TableColumn<CellSummary, String> percentageColumn;

    private Image originalImage;
    private PixelReader pixelReader;
    private int width, height;
    private int[] cellArray;
    private MyUnionFind uf;
    private HashMap<Integer, BloodCellDetails> cells = new HashMap<>();
    private boolean lastAnalyzed = false;

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setImageArray(int[] cellArray) { this.cellArray = cellArray; }
    public void setPills(HashMap<Integer, BloodCellDetails> map) { this.cells = map; }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainController = this;

        cellTypeColumn.setCellValueFactory(data -> data.getValue().cellTypeProperty());
        countColumn.setCellValueFactory(data -> data.getValue().countProperty().asObject());
        percentageColumn.setCellValueFactory(data -> data.getValue().percentageProperty());
    }

    @FXML
    public void openImage() {
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            originalImage = new Image(file.toURI().toString());
            originalView.setImage(originalImage);
            dropImageHint.setVisible(false);
            pixelReader = originalImage.getPixelReader();
            width = (int) originalImage.getWidth();
            height = (int) originalImage.getHeight();
            cellArray = new int[width * height];
            uf = new MyUnionFind(width * height);
            infoList.getItems().clear();
            resultView.setImage(null);
            tricolorView.setImage(null);
            lastAnalyzed = false;
        }
    }

    @FXML
    public void convertToTricolor() {
        WritableImage tricolor = new WritableImage(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = pixelReader.getColor(x, y);
                double hue = c.getHue();
                double sat = c.getSaturation();
                double bright = c.getBrightness();
                int index = y * width + x;

                if ((hue > 330 || hue < 20) && sat > 0.2 && bright > 0.4) {
                    tricolor.getPixelWriter().setColor(x, y, Color.RED);
                    cellArray[index] = -1;
                } else if ((hue > 200 && hue < 280) && sat > 0.4 && bright < 0.8) {
                    tricolor.getPixelWriter().setColor(x, y, Color.PURPLE);
                    cellArray[index] = -2;
                } else {
                    tricolor.getPixelWriter().setColor(x, y, Color.WHITE);
                    cellArray[index] = 0;
                }
            }
        }

        tricolorView.setImage(tricolor);
    }

    @FXML
    public void analyzeCells() {
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

        Map<Integer, List<Integer>> clusters = new HashMap<>();
        for (int i = 0; i < cellArray.length; i++) {
            if (cellArray[i] < 0) {
                int root = uf.find(i);
                clusters.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
            }
        }

        WritableImage result = new WritableImage(width, height);
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(originalImage, 0, 0);

        int redCount = 0, whiteCount = 0, label = 1;
        cells.clear();

        for (Map.Entry<Integer, List<Integer>> entry : clusters.entrySet()) {
            List<Integer> indices = entry.getValue();
            int size = indices.size();
            if (size < 20) continue;

            int minX = width, maxX = 0, minY = height, maxY = 0;
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

            BloodCellDetails cell = new BloodCellDetails(type, label++, size, entry.getKey(), rectColor);
            cell.getIndices().addAll(indices);
            cells.put(entry.getKey(), cell);

            gc.setStroke(rectColor);
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
        lastAnalyzed = true;

        infoList.getItems().clear();
        infoList.getItems().add("Estimated Red Cells: " + redCount);
        infoList.getItems().add("Estimated White Cells: " + whiteCount);
        infoList.getItems().add("Total Clusters: " + cells.size());

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
    }

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

    public Image getOriginalImage() {
        return originalImage;
    }
}
