package org.example.grid;

import java.util.*;

public class KNN {
    private int         k;
    private List<Point> trainingData;

    public KNN(int k) {
        this.k = k;
        this.trainingData = new ArrayList<>();
    }

    public void train(List<Point> points, List<AddedPoint> aPoints) {
        trainingData.clear();
        trainingData.addAll(points);
        trainingData.addAll(aPoints);
    }

    private void validateState(){
        if (trainingData.isEmpty()) {
            throw new IllegalStateException("No training data.");
        }
    }

    public List<Point> getEnvironment(int x, int y){
        validateState();

        trainingData.sort((p1,p2) -> {return Double.compare(Point.distBetween(p1,x,y),
                                                                       Point.distBetween(p2,x,y));});
        if (trainingData.size() <= k) return new ArrayList<>(trainingData);
        else                          return new ArrayList<>(trainingData.subList(0,k));
    }

    public Point.ClassLabel predictClsLabel(int x, int y) {
        validateState();

        List <Point> surroundings = getEnvironment(x, y);
        Map<Point.ClassLabel, Integer> labelCount = new HashMap<>();
        for(Point p0 : surroundings) {
            Point.ClassLabel label = p0.getClsLabel();
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }

        return Collections.max(labelCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}
