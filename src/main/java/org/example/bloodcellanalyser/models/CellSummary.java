package org.example.bloodcellanalyser.models; // Declares the class belongs to the 'models' package

// Import JavaFX property types used for data binding in UI components like TableView
import javafx.beans.property.*;

public class CellSummary {

    // Property representing the type of cell ("Red Cells", "White Cells", etc.)
    private final StringProperty cellType;

    // Property representing the number of cells of this type
    private final IntegerProperty count;

    // Property representing the percentage of this cell type (e.g., "43.2%")
    private final StringProperty percentage;

    // Constructor that initializes the properties using JavaFX's property wrappers
    public CellSummary(String cellType, int count, String percentage) {
        // Wrap the plain String value with a JavaFX property for automatic UI updates
        this.cellType = new SimpleStringProperty(cellType);

        // Wrap the plain integer value with a JavaFX property
        this.count = new SimpleIntegerProperty(count);

        // Wrap the percentage string with a JavaFX property
        this.percentage = new SimpleStringProperty(percentage);
    }

    // Getter method for cell type (returns the actual string value from the property)
    public String getCellType() { return cellType.get(); }

    // Getter method for cell count (returns the actual integer value from the property)
    public int getCount() { return count.get(); }

    // Getter method for percentage string
    public String getPercentage() { return percentage.get(); }

    // Property getter for cell type (used to bind UI TableView column to this property)
    public StringProperty cellTypeProperty() { return cellType; }

    // Property getter for count (used to bind UI TableView column to this property)
    public IntegerProperty countProperty() { return count; }

    // Property getter for percentage (used to bind UI TableView column to this property)
    public StringProperty percentageProperty() { return percentage; }
}
