package org.example;

public class Point {
    private final double x;
    private final double y;
    private String label;

    public Point(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getLabel() { return label; }
    public void setLabel(String l) { this.label = l; }
}