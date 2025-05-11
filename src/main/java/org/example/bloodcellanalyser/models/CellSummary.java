package org.example.bloodcellanalyser.models;

import javafx.beans.property.*;

public class CellSummary {
    private final StringProperty cellType;
    private final IntegerProperty count;
    private final StringProperty percentage;

    public CellSummary(String cellType, int count, String percentage) {
        this.cellType = new SimpleStringProperty(cellType);
        this.count = new SimpleIntegerProperty(count);
        this.percentage = new SimpleStringProperty(percentage);
    }

    public String getCellType() { return cellType.get(); }
    public int getCount() { return count.get(); }
    public String getPercentage() { return percentage.get(); }

    public StringProperty cellTypeProperty() { return cellType; }
    public IntegerProperty countProperty() { return count; }
    public StringProperty percentageProperty() { return percentage; }
}
