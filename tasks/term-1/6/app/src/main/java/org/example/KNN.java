package org.example;
import java.util.*;

public class KNN {
    private List<Point> pointList;
    private int k;

    public KNN(List<Point> pointList, int k) {
        this.pointList = pointList;
        this.k = k;
    }

    private double distance(Point a, double[] b) {
        return Math.sqrt((a.x - b[0]) * (a.x - b[0]) + (a.y - b[1]) * (a.y - b[1]));
    }

    public String classify(double[] point){
        List<Map.Entry<Double,String>> dist = new ArrayList<>();
        for (Point j : pointList){
            dist.add(Map.entry(distance(j, point), j.label));
        }
        dist.sort(Comparator.comparingDouble(Map.Entry::getKey));

        Map<String, Integer> votes = new HashMap<>();
        for (int i = 0; i < k; i++){
            String lab = dist.get(i).getValue();
            votes.put(lab, votes.getOrDefault(lab,0) +1);
        }
        return votes.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }
}
