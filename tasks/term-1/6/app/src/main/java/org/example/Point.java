package org.example;

public class Point {
    private final double x;
    private final double y;
    private final String label;

    public Point(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public Point(double x, double y) {
        this(x, y, null);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getLabel() { return label; }

    public double distanceTo(Point other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)%s", x, y, label != null ? " [" + label + "]" : "");
    }
}