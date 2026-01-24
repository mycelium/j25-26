package org.example.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

public class Space {

    private List<Point> pointList = new ArrayList<>();

    public static final int DEFAULT_K = 3;
    
    public Space(){}

    private double distance(Point p1, Point p2){

        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }

    public List<Point> getPoints(){
        return new ArrayList<>(pointList);
    }

    public Queue<PointWithDistance> findKNearestPoints(Point point){
    
        Queue<PointWithDistance> kNearestPoints = new PriorityQueue<>(
            DEFAULT_K + 1,
            (p1, p2) -> Double.compare(p2.distance(), p1.distance())
        );

        for (int i = 0; i < pointList.size(); i++) {
            
            double distance = distance(point, pointList.get(i));
            kNearestPoints.offer(new PointWithDistance(pointList.get(i), distance));

            if (kNearestPoints.size() > DEFAULT_K) {
                kNearestPoints.poll();
            }
        }

        return kNearestPoints;
    }

    public void addPoint(Point point) { pointList.add(point); }

    public String classify(Point point){

        Queue<PointWithDistance> kNearestPoints = findKNearestPoints(point);
        Map<String, Integer> labelAmount = new HashMap<>();

        kNearestPoints.forEach(p -> {
            String Label = p.point().getLabel();
            labelAmount.merge(Label, 1, Integer::sum);
        });
        
        int maxCount = Collections.max(labelAmount.values());

        List<String> maxLabels = labelAmount.entrySet().stream()
                        .filter(e -> e.getValue() == maxCount)
                        .map(e -> e.getKey())
                        .collect(Collectors.toList());

        if (maxLabels.size() > 1) {
            return resolveTie(kNearestPoints, maxLabels);
        }
        
        return maxLabels.get(0);
    }

    private String resolveTie(Queue<PointWithDistance> nearestPoints, List<String> tiedClasses) {
        Map<String, Double> weightedVotes = new HashMap<>();
        
        for (PointWithDistance pwd : nearestPoints) {
            String Label = pwd.point().getLabel();
            if (tiedClasses.contains(Label)) {
                double weight = 1.0 / (1.0 + pwd.distance());
                weightedVotes.put(Label, weightedVotes.getOrDefault(Label, 0.0) + weight);
            }
        }
        
        return Collections.max(weightedVotes.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}
