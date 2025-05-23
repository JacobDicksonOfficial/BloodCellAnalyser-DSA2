package org.example.bloodcellanalyser.models;

import javafx.scene.paint.Color;
import java.util.ArrayList;

public class BloodCellDetails {

    private String type;
    private int label;
    private int size;
    private int root;
    private Color color;
    private ArrayList<Integer> indices = new ArrayList<>();

    // ➕ Bounding box fields for hover detection
    private int minX, minY, maxX, maxY;

    public BloodCellDetails(String type, int label, int size, int root, Color color) {
        this.type = type;
        this.label = label;
        this.size = size;
        this.root = root;
        this.color = color;
    }

    public String getType() { return type; }
    public int getLabel() { return label; }
    public int getSize() { return size; }
    public int getRoot() { return root; }
    public Color getColor() { return color; }
    public ArrayList<Integer> getIndices() { return indices; }

    public void setType(String type) { this.type = type; }
    public void setLabel(int label) { this.label = label; }
    public void setSize(int size) { this.size = size; }
    public void setRoot(int root) { this.root = root; }
    public void setColor(Color color) { this.color = color; }

    // ➕ Sets the bounding box for this cell cluster
    public void setBounds(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    // ➕ Checks if a point (x, y) is within this cell's bounding box
    public boolean contains(int x, int y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    @Override
    public String toString() {
        String str = "BloodCellDetails{" +
                "type='" + type + '\'' +
                ", label=" + label +
                ", size=" + size +
                ", root=" + root +
                '}';
        for (Integer i : indices) {
            str += " " + i;
        }
        return str;
    }
}
