package org.example.bloodcellanalyser; // Defines the package this class belongs to

// Import required JavaFX and project-specific classes
import javafx.scene.paint.Color; // Used only if you extend to color-aware benchmarks
import org.example.bloodcellanalyser.controllers.MainController; // Used to simulate GUI controller context
import org.example.bloodcellanalyser.models.BloodCellDetails; // (Imported but unused here)

// Import JMH benchmarking annotations and utilities
import org.openjdk.jmh.annotations.*;

import java.util.HashMap; // Used to simulate storage for blood cell clusters
import java.util.Random; // Used for generating random pixel data
import java.util.concurrent.TimeUnit; // Specifies benchmark timing units

// Specifies the type of benchmark (average execution time per operation)
@BenchmarkMode(Mode.AverageTime)

// Specifies the time unit used to display benchmark results
@OutputTimeUnit(TimeUnit.MILLISECONDS)

// Tells JMH to create a new instance of this class per thread
@State(Scope.Thread)

// Number of warm-up iterations before measurement
@Warmup(iterations = 1)

// Number of iterations for actual measurement
@Measurement(iterations = 1)

// Number of forks (how many JVMs to launch per benchmark)
@Fork(1)
public class BloodCellBenchmark {

    // Image width and height for simulated pixel grid
    private final int width = 256;
    private final int height = 256;

    // Array that stores simulated pixel classifications
    private int[] cellArray;

    // Union-Find data structure for grouping pixels
    private MyUnionFind uf;

    // Setup method run before each benchmark invocation
    @Setup(Level.Invocation)
    public void setup() {
        Random rand = new Random(); // Create a random number generator

        cellArray = new int[width * height]; // Allocate 1D pixel array
        uf = new MyUnionFind(width * height); // Create a new Union-Find structure

        // Randomly classify pixels as -1 (active/targeted) or 0 (background)
        for (int i = 0; i < cellArray.length; i++) {
            cellArray[i] = rand.nextDouble() > 0.9 ? -1 : 0; // ~10% chance to be active
        }

        // Simulate MainController setup (mimics how GUI app initializes)
        MainController.mainController = new MainController();
        MainController.mainController.setWidth(width);
        MainController.mainController.setHeight(height);
        MainController.mainController.setImageArray(cellArray);
        MainController.mainController.setPills(new HashMap<>()); // Empty cell details
    }

    // Benchmark: Perform union operations on horizontally adjacent active pixels
    @Benchmark
    public void benchmarkUnionFind() {
        for (int i = 0; i < width * height - 1; i++) {
            // If both current and next pixels are marked active (-1), union them
            if (cellArray[i] == -1 && cellArray[i + 1] == -1) {
                uf.union(i, i + 1); // Merge clusters
            }
        }
    }

    // Benchmark: Run find operation on every pixel (regardless of value)
    @Benchmark
    public void benchmarkFind() {
        for (int i = 0; i < width * height; i++) {
            uf.find(i); // Query root for each pixel index
        }
    }

    // Main method to launch the benchmark using JMH
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args); // Entry point for running benchmarks
    }
}
