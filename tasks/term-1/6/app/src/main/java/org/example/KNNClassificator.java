package org.example;

import java.util.*;

public  class KNNClassificator {
    private final List<Point> pntList;
    private final int k;
    private List<Point> testPoints;
    public KNNClassificator(int k) {
        this.pntList = new ArrayList<>();
        this.k = k;
        this.testPoints = new ArrayList<>();
    }
    public void addPoints(List<Point> points) {
        pntList.addAll(points);
    }
    public void createData(double[] lX, double[] lY)
    {
        if (lX.length != lY.length) return;
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < lX.length; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                for (int n = 0; n < 4; n++)
                {
                    double x = lX[i] + (j);
                    double y = lY[i] + (n);
                    points.add(new Point(x, y, i));
                }
            }
        }
        addPoints(points);
    }
    public Integer classifyPoint(double x, double y) {
        Point testPoint = new Point(x, y, null);
        List<Distance> distances = new ArrayList<>();
        for (Point point : pntList) {
            double distance = getDistance(point, testPoint);
            distances.add(new Distance(distance, point.label()));
        }
        distances.sort(Comparator.comparingDouble(Distance::distance));
        Map<Integer, Integer> votes = new HashMap<>();
        for (int i = 0; i < Math.min(k, distances.size()); i++) {
            Integer label = distances.get(i).label();
            votes.put(label, votes.getOrDefault(label, 0) + 1);
        }
        Integer label = null;
        int maxVotes = 0;
        for (Map.Entry<Integer, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                label = entry.getKey();
            }
        }
        this.testPoints.add(new Point(x, y, label));
        return label;
    }
    public void visualize() {
        KNNVisualizer visualizer = new KNNVisualizer(pntList, testPoints);
        visualizer.visualizeAndSave("knn.png");
    }
    private double getDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private record Distance(double distance, Integer label) {}
    public record Point(double x, double y, Integer label){}
}