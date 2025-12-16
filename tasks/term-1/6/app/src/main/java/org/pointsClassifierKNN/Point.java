package org.pointsClassifierKNN;

public class Point {
    private double x, y;
    private String label;

    public Point(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public Point(double x, double y) {
        this(x, y, null);
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

    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f): %s", x, y, label == null ? "?" : label);
    }
}