package org.knn;
import java.util.*;

public class KNN {
    private List<Point> trainingData;
    private int trainingPoint;

    public KNN(List<Point> pointList, int k) {
        this.trainingData = pointList;
        this.trainingPoint = k;
    }

    private double distance(Point a, double[] b) {
        return Math.sqrt((a.x - b[0]) * (a.x - b[0]) + (a.y - b[1]) * (a.y - b[1]));
    }

    public String classify(double[] point){
        List<Map.Entry<Double,String>> distances = new ArrayList<>();
        for (Point j : trainingData){
            distances.add(Map.entry(distance(j, point), j.label));
        }
        distances.sort(Comparator.comparingDouble(Map.Entry::getKey));

        Map<String, Integer> votes = new HashMap<>();
        for (int i = 0; i < trainingPoint; i++){
            String lab = distances.get(i).getValue();
            votes.put(lab, votes.getOrDefault(lab,0) +1);
        }
        return votes.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }
}
