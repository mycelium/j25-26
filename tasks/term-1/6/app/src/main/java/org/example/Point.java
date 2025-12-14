package org.example;

public class Point {
    private double x;
    private double y;
    private String label;

    public Point(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.label = null;
    }

    // Рассчитывает евклидово расстояние до другой точки
    public double distanceTo(Point other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @Override
    public String toString() {
        return String.format("Point(%.2f, %.2f) -> %s", x, y, label);
    }
}