package org.example.bloodcellanalyser.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.bloodcellanalyser.MyUnionFind;
import org.example.bloodcellanalyser.models.BloodCellDetails;

import java.io.File;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    public static MainController mainController;
    private FileChooser fileChooser = new FileChooser();

    @FXML private ImageView originalView, tricolorView, resultView;
    @FXML private Slider hueS, satS, brightS, maxSatS, maxBrightS;
    @FXML private ListView<String> infoList;
    @FXML private ToggleButton toggleLabels;
    @FXML private Button analyzeBtn;

    private Image originalImage;
    private PixelReader pixelReader;
    private int width, height;
    private int[] cellArray;
    private MyUnionFind uf;
    private HashMap<Integer, BloodCellDetails> cells = new HashMap<>();
    private WritableImage lastResultImage;
    private boolean lastAnalyzed = false;

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setImageArray(int[] cellArray) { this.cellArray = cellArray; }
    public void setPills(HashMap<Integer, BloodCellDetails> map) { this.cells = map; }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainController = this;
    }

    @FXML
    public void openImage() {
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            originalImage = new Image(file.toURI().toString());
            originalView.setImage(originalImage);
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

        List<Map.Entry<Integer, List<Integer>>> sortedClusters = new ArrayList<>(clusters.entrySet());
        sortedClusters.sort(Comparator.comparingInt(entry -> {
            List<Integer> indices = entry.getValue();
            int minY = indices.stream().mapToInt(i -> i / width).min().orElse(0);
            int minX = indices.stream().mapToInt(i -> i % width).min().orElse(0);
            return minY * width + minX;
        }));

        WritableImage result = new WritableImage(width, height);
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(originalImage, 0, 0);

        int redCount = 0, whiteCount = 0, label = 1;
        cells.clear();

        for (Map.Entry<Integer, List<Integer>> entry : sortedClusters) {
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
        lastResultImage = result;
        lastAnalyzed = true;

        infoList.getItems().clear();
        infoList.getItems().add("Estimated Red Cells: " + redCount);
        infoList.getItems().add("Estimated White Cells: " + whiteCount);
        infoList.getItems().add("Total Clusters: " + cells.size());
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

    public Image getOriginalImage() {
        return originalImage;
    }
}
