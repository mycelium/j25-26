package org.example;

import java.util.*;
//import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;

public class KNNClassifier
{
    private List<Point> trainingData;
    private int k;

    public KNNClassifier(List<Point> trainingData, int k)
    {
        this.trainingData = new ArrayList<>(trainingData);
        this.k = k;

        if (k <= 0 || k >= 20)
        {
            throw new IllegalArgumentException("K must be between (1) and (2 * number of elements in a class)");
        }

        if (trainingData.isEmpty())
        {
            throw new IllegalArgumentException("Training data can't be empty");
        }
    }

    public String classifyNewPoint(Point newPoint)
    {
        List<Point> sortedData = new ArrayList<>(trainingData);
        sortedData.sort(Comparator.comparingDouble(p -> p.calculateDistance(newPoint)));

        List<Point> kNearestNeighbors = sortedData.subList(0, k);
        Map<String, Integer> nearestClasses = new HashMap<>();

        for (Point neighbors: kNearestNeighbors)
        {
            String className = neighbors.getPointClass();
            nearestClasses.put(className, nearestClasses.getOrDefault(className, 0) + 1);
        }

        String classNewPoint = Collections.max(nearestClasses.entrySet(), Map.Entry.comparingByValue()).getKey();
        newPoint.setPointClass(classNewPoint);
        return classNewPoint;
    }

    public List<Point> getTrainingData()
    {
        return trainingData;
    }

}
