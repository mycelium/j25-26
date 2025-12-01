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

    public double getX() { return x; }
    public double getY() { return y; }
    public String getLabel() { return label; }
}
