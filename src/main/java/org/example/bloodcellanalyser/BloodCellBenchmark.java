package org.example.bloodcellanalyser;

import javafx.scene.paint.Color;
import org.example.bloodcellanalyser.controllers.MainController;
import org.example.bloodcellanalyser.models.BloodCellDetails;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BloodCellBenchmark {

    int width = 256, height = 256;
    MyUnionFind uf = new MyUnionFind(width * height);
    int[] cellArray = new int[width * height];

    @Setup(Level.Invocation)
    public void setup() {
        Random rand = new Random();
        MainController.mainController = new MainController();
        MainController.mainController.setWidth(width);
        MainController.mainController.setHeight(height);

        for (int i = 0; i < cellArray.length; i++) {
            cellArray[i] = (rand.nextDouble() > 0.9) ? -1 : 0;
        }

        MainController.mainController.setImageArray(cellArray);
        MainController.mainController.setPills(new HashMap<>());
    }

    @Benchmark
    public void benchmarkUnionFind() {
        for (int i = 0; i < width * height - 1; i++) {
            if (cellArray[i] == -1 && cellArray[i + 1] == -1) {
                uf.union(i, i + 1);
            }
        }
    }

    @Benchmark
    public void benchmarkFind() {
        for (int i = 0; i < width * height; i++) {
            uf.find(i);
        }
    }

    public static void main(String[] args) throws Exception {
        Main.main(args);
    }
}
