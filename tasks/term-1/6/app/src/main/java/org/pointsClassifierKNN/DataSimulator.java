package org.pointsClassifierKNN;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataSimulator {
    private Random rand;

    public DataSimulator(long seed) {
        this.rand = new Random(seed);
    }

    public List<Point> generateCircularClusters(int classes, int pointsPerClass, double spread) {
        List<Point> result = new ArrayList<>();
        String[] labels = makeLabels(classes);

        for (int c = 0; c < classes; c++) {
            double angle = 2 * Math.PI * c / classes;
            double cx = 0.5 + 0.3 * Math.cos(angle);
            double cy = 0.5 + 0.3 * Math.sin(angle);

            for (int i = 0; i < pointsPerClass; i++) {
                double x = cx + rand.nextGaussian() * spread;
                double y = cy + rand.nextGaussian() * spread;
                result.add(new Point(x, y, labels[c]));
            }
        }
        return result;
    }

    public List<Point> generateCornerGroups(int classes, int pointsPerClass) {
        if (classes != 4) {
            throw new IllegalArgumentException("requires 4 classes.");
        }
        List<Point> result = new ArrayList<>();
        String[] labels = {"Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right"};
        double[][] centers = {{0.2, 0.8}, {0.8, 0.8}, {0.2, 0.2}, {0.8, 0.2}};

        for (int c = 0; c < 4; c++) {
            double cx = centers[c][0];
            double cy = centers[c][1];
            for (int i = 0; i < pointsPerClass; i++) {
                double x = cx + (rand.nextDouble() - 0.5) * 0.4;
                double y = cy + (rand.nextDouble() - 0.5) * 0.4;
                result.add(new Point(x, y, labels[c]));
            }
        }
        return result;
    }

    public List<Point> generateDiagonalStripes(int classes, int totalPoints) {
        List<Point> result = new ArrayList<>();
        String[] labels = makeLabels(classes);
        double stripeWidth = 0.15;
        double offsetStep = 0.4;

        for (int i = 0; i < totalPoints; i++) {
            int cls = rand.nextInt(classes);
            double baseOffset = -0.3 + cls * offsetStep;
            double x = rand.nextDouble();
            double y = x + baseOffset + (rand.nextDouble() - 0.5) * stripeWidth;

            x = Math.max(0.0, Math.min(1.0, x));
            y = Math.max(0.0, Math.min(1.0, y));

            result.add(new Point(x, y, labels[cls]));
        }
        return result;
    }

    private String[] makeLabels(int n) {
        String[] arr = new String[n];
        for (int i = 0; i < n; i++) {
            arr[i] = String.valueOf((char)('A' + i));
        }
        return arr;
    }

    public List<Point> generateRandomPoints(int n) {
        List<Point> pts = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double x = rand.nextDouble();
            double y = rand.nextDouble();
            pts.add(new Point(x, y));
        }
        return pts;
    }
}