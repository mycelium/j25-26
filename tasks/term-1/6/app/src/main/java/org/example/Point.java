
package org.example;

public class Point {
    private double x;
    private double y;
    private String label;
    private double distanceToTarget;
    
    public Point() {
    }
    
    public Point(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.distanceToTarget = 0.0;
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
    
    public double getDistanceToTarget() {
        return distanceToTarget;
    }
    
    public void setDistanceToTarget(double distanceToTarget) {
        this.distanceToTarget = distanceToTarget;
    }
    
    @Override
    public String toString() {
        return String.format("Point{x=%.2f, y=%.2f, label='%s', distance=%.2f}", 
                           x, y, label, distanceToTarget);
    }
}