package org.example.bloodcellanalyser.models; // Declares the package for this test class

// Imports the JavaFX Color class used in the BloodCellDetails constructor
import javafx.scene.paint.Color;

// Imports JUnit lifecycle and test annotations
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Imports utility class to simplify working with lists
import java.util.Arrays;

// Imports JUnit assertion methods for validating test expectations
import static org.junit.jupiter.api.Assertions.*;

// Unit test class for the BloodCellDetails model
public class BloodCellDetailsTest {

    // Field to hold the instance of BloodCellDetails for use in tests
    private BloodCellDetails cell;

    // Setup method that runs before each test, initializing a default BloodCellDetails instance
    @BeforeEach
    public void setUp() {
        // Creates a red cell with label=1, size=25, root=5, and color RED
        cell = new BloodCellDetails("red", 1, 25, 5, Color.RED);
    }

    // Test to verify that the constructor and all getter methods work correctly
    @Test
    public void testConstructorAndGetters() {
        assertEquals("red", cell.getType());               // Check type
        assertEquals(1, cell.getLabel());                  // Check label
        assertEquals(25, cell.getSize());                  // Check size
        assertEquals(5, cell.getRoot());                   // Check root ID
        assertEquals(Color.RED, cell.getColor());          // Check color
        assertTrue(cell.getIndices().isEmpty());           // Check that indices list starts empty
    }

    // Test that verifies each setter updates the value correctly and getter reflects the new value
    @Test
    public void testSetters() {
        cell.setType("white");
        cell.setLabel(2);
        cell.setSize(100);
        cell.setRoot(10);
        cell.setColor(Color.BLUE);

        assertEquals("white", cell.getType());
        assertEquals(2, cell.getLabel());
        assertEquals(100, cell.getSize());
        assertEquals(10, cell.getRoot());
        assertEquals(Color.BLUE, cell.getColor());
    }

    // Test that ensures the indices list correctly stores and returns pixel positions
    @Test
    public void testIndicesList() {
        cell.getIndices().addAll(Arrays.asList(1, 2, 3)); // Add three pixel indices
        assertEquals(3, cell.getIndices().size());        // List size should be 3
        assertEquals(Arrays.asList(1, 2, 3), cell.getIndices()); // List should contain the same elements
    }

    // Test that verifies the toString() method includes all key fields and pixel indices
    @Test
    public void testToStringIncludesKeyFieldsAndIndices() {
        cell.getIndices().addAll(Arrays.asList(10, 11)); // Add two indices
        String result = cell.toString();                 // Generate the string representation

        // Ensure the string includes all relevant field values and indices
        assertTrue(result.contains("type='red"));
        assertTrue(result.contains("label=1"));
        assertTrue(result.contains("size=25"));
        assertTrue(result.contains("root=5"));
        assertTrue(result.contains("10"));
        assertTrue(result.contains("11"));
    }
}
