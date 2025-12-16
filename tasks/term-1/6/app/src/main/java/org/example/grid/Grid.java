package org.example.grid;

import org.example.generator.Generator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Grid extends JPanel{

    private int xLength;
    private int yLength;
    private List<Point> generatedPoints;
    private List<AddedPoint> addedPoints = new ArrayList<>();
    private KNN knn = new KNN(5);

    public Grid(int aXLength, int aYLength, Generator gen) {
        if (aXLength < 1 || aYLength < 1)
            throw new IllegalArgumentException("Illegal argument");

        xLength = aXLength;
        yLength = aYLength;
        generatedPoints = gen.generate(xLength, yLength);
    }

    public int         getXLength() { return xLength; }
    public int         getYLength() { return yLength; }
    public List<Point> getGeneratedPoints()  { return generatedPoints; }
    public List<AddedPoint> getAPoints()  { return addedPoints; }

    public void reGenerate(Generator gen) {
        generatedPoints.clear();
        addedPoints.clear();
        generatedPoints = gen.generate(xLength,yLength);
    }

    public void addPoint(int x, int y){
        if (y < 1 || x < 1)
            throw new IllegalArgumentException("Illegal argument");

        knn.train(generatedPoints, addedPoints);

        AddedPoint newAP = new AddedPoint(x,y,
                                          knn.predictClsLabel(x, y),
                                          knn.getEnvironment(x,y));
        addedPoints.add(newAP);
    }
}
