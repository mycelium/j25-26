package org.example.classifier;

public class DataPoint {
    private double x;
    private double y;
    private String label;

    public DataPoint(double x, double y) {
        this(x, y, null);
    }

    public DataPoint(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getLabel() {
        return label;
    }

    public double distanceTo(DataPoint other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}