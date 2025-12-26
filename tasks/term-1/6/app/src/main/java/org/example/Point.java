package org.example;

public class Point {

    private double x;
    private double y;
    private String label;
    private double distanceToQuery;

    public Point() {
        this(0.0, 0.0, null);
    }

    public Point(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.distanceToQuery = 0.0;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getDistanceToQuery() {
        return distanceToQuery;
    }

    public void setDistanceToQuery(double distanceToQuery) {
        this.distanceToQuery = distanceToQuery;
    }

    public boolean hasLabel() {
        return label != null && !label.isEmpty();
    }

    @Override
    public String toString() {
        String lbl = label == null ? "<unlabeled>" : label;
        return String.format("(%.4f, %.4f) -> %s [d=%.5f]",
                x, y, lbl, distanceToQuery);
    }
}