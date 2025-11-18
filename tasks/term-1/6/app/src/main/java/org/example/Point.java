package org.example;

public class Point {
    private double x;
    private double y;
    private String classLabel;

    public Point(double x, double y, String classLabel) {
        this.x = x;
        this.y = y;
        this.classLabel = classLabel;
    } 

    public Point(double x,  double y) {
        this(x, y, null);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getLabel() {
        return classLabel;
    }

    public double dist(Point point) {
        double distanceX = this.x - point.x;
        double distanceY = this.y - point.y;

        return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f) – %s", x, y, classLabel != null ? classLabel : "none");
    }

}