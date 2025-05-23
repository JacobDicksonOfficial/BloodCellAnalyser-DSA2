package org.example.bloodcellanalyser.models; // Declares this class is part of the 'models' package

// Import JavaFX Color class used to store the cell's color (used for drawing bounding boxes)
import javafx.scene.paint.Color;

// Import ArrayList for storing pixel indices that belong to this blood cell
import java.util.ArrayList;

// Model class representing details about a single blood cell cluster
public class BloodCellDetails {

    // Type of cell: "red", "white", or "red-cluster"
    private String type;

    // A unique label number assigned to this cell (for UI display)
    private int label;

    // Number of pixels that make up this cell (cluster size)
    private int size;

    // Root ID from Union-Find structure, identifies the cluster this cell belongs to
    private int root;

    // Color used to draw this cell's bounding box in the result image
    private Color color;

    // List of all pixel indices (flattened x+y) that belong to this cell
    private ArrayList<Integer> indices = new ArrayList<>();

    // Constructor that initializes all the fields (except indices, which starts empty)
    public BloodCellDetails(String type, int label, int size, int root, Color color) {
        this.type = type;
        this.label = label;
        this.size = size;
        this.root = root;
        this.color = color;
    }

    // Getter method to retrieve the cell type
    public String getType() {
        return type;
    }

    // Getter method to retrieve the label number
    public int getLabel() {
        return label;
    }

    // Getter method to retrieve the cluster size (number of pixels)
    public int getSize() {
        return size;
    }

    // Getter method to retrieve the Union-Find root ID
    public int getRoot() {
        return root;
    }

    // Getter method to retrieve the color assigned to this cell
    public Color getColor() {
        return color;
    }

    // Getter method to retrieve the list of pixel indices that form this cell
    public ArrayList<Integer> getIndices() {
        return indices;
    }

    // Setter method to update the cell type
    public void setType(String type) {
        this.type = type;
    }

    // Setter method to update the label
    public void setLabel(int label) {
        this.label = label;
    }

    // Setter method to update the cluster size
    public void setSize(int size) {
        this.size = size;
    }

    // Setter method to update the Union-Find root ID
    public void setRoot(int root) {
        this.root = root;
    }

    // Setter method to update the color used for display
    public void setColor(Color color) {
        this.color = color;
    }

    // Returns a string summary of the blood cell for debugging/logging
    @Override
    public String toString() {
        // Start with main field values
        String str = "BloodCellDetails{" +
                "type='" + type + '\'' +
                ", label=" + label +
                ", size=" + size +
                ", root=" + root +
                '}';

        // Append all pixel indices to the string
        for (Integer i : indices) {
            str += " " + i;
        }

        return str; // Final string representation
    }
}
