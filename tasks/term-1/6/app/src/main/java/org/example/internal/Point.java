package org.example.internal;

public class Point {
    private double x;
    private double y;
    private String classLabel = null;
    
    public Point(double x, double y){

        this.x = x;
        this.y = y;
    }

    public Point(double x, double y, String label){

        this.x = x;
        this.y = y;
        this.classLabel = label;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getLabel() { return classLabel; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setX(String Label) { this.classLabel = Label; }
}   
