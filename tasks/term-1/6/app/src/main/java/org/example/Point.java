package org.example;

public class Point {

    private double x;
    private double y;
    private int classLabel;

    public Point(double x, double y, int classLabel) {
        this.x = x;
        this.y = y;
        this.classLabel = classLabel;
    }

    public Point(double x, double y) {
        this(x, y, -1);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getClassLabel() { return classLabel; }


    public double EuclidDist(Point other)
    {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

}

