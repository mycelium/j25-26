package org.example;

import java.util.*;

public class DataSet {

    private final List<Point> data = new ArrayList<>();

    public DataSet() {
    }

    public DataSet(List<Point> source) {
        if (source != null) {
            data.addAll(source);
        }
    }

    public void add(Point p) {
        if (p != null) {
            data.add(p);
        }
    }

    public List<Point> getPoints() {
        return new ArrayList<>(data);
    }

    public int size() {
        return data.size();
    }

    public List<String> getUniqueLabels() {
        Set<String> labels = new HashSet<>();
        for (Point p : data) {
            if (p != null && p.hasLabel()) {
                labels.add(p.getLabel());
            }
        }
        List<String> list = new ArrayList<>(labels);
        Collections.sort(list);
        return list;
    }

    public static DataSet buildClusteredSet() {
        List<Point> pts = new ArrayList<>();
        Random rnd = new Random(101);

        addGaussianCluster(pts, rnd, 0.25, 0.25, 0.05, 0.04, 45, "Class A");
        addGaussianCluster(pts, rnd, 0.70, 0.55, 0.06, 0.05, 45, "Class B");
        addGaussianCluster(pts, rnd, 0.30, 0.80, 0.05, 0.05, 45, "Class C");

        return new DataSet(pts);
    }

    private static void addGaussianCluster(List<Point> dst, Random rnd, double centerX, double centerY, double spreadX,
                                           double spreadY, int count, String label) {
        for (int i = 0; i < count; i++) {
            double x = centerX + rnd.nextGaussian() * spreadX;
            double y = centerY + rnd.nextGaussian() * spreadY;
            dst.add(new Point(x, y, label));
        }
    }

    public static DataSet buildGridWithCenter() {
        List<Point> pts = new ArrayList<>();

        fillGrid(pts, 0.10, 0.10, 0.05, 0.05, 5, 5, "Class NW");
        fillGrid(pts, 0.65, 0.10, 0.05, 0.05, 5, 5, "Class NE");
        fillGrid(pts, 0.10, 0.65, 0.05, 0.05, 5, 5, "Class SW");
        fillGrid(pts, 0.65, 0.65, 0.05, 0.05, 5, 5, "Class SE");

        fillGrid(pts, 0.40, 0.40, 0.04, 0.04, 6, 6, "Class Center");

        return new DataSet(pts);
    }

    private static void fillGrid(List<Point> dst, double startX, double startY, double stepX, double stepY,
                                 int cols, int rows, String label) {
        for (int ix = 0; ix < cols; ix++) {
            for (int iy = 0; iy < rows; iy++) {
                double x = startX + ix * stepX;
                double y = startY + iy * stepY;
                dst.add(new Point(x, y, label));
            }
        }
    }

    public static DataSet buildRandomCloud(int classCount, int totalPoints) {
        if (classCount <= 0) {
            throw new IllegalArgumentException("classCount must be > 0");
        }
        if (totalPoints <= 0) {
            throw new IllegalArgumentException("totalPoints must be > 0");
        }

        String[] labels = new String[classCount];
        for (int i = 0; i < classCount; i++) {
            labels[i] = "Class " + (char) ('A' + i);
        }

        List<Point> pts = new ArrayList<>(totalPoints);
        Random rnd = new Random(555);

        for (int i = 0; i < totalPoints; i++) {
            double x = rnd.nextDouble();
            double y = rnd.nextDouble();
            String lbl = labels[rnd.nextInt(classCount)];
            pts.add(new Point(x, y, lbl));
        }

        return new DataSet(pts);
    }
}