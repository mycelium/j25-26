package org.example;

import org.example.generator.*;

import java.util.Arrays;

public class App {
    public static void main(String[] args) {

        try {
            int xLength = 600;
            int yLength = 380;
            Visualizer vslzr = new Visualizer(xLength, yLength,
                                              Arrays.asList(new GeneratorUnifDist(),
                                                            new GeneratorXORDist()));
            vslzr.visualize();
        }
        catch (Exception e)
        {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
