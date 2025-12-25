package org.example;

public class Point
{
    private double x;
    private double y;
    private String pointClass;

    public Point(double x, double y, String pointClass)
    {
        this.x = x;
        this.y = y;
        this.pointClass = pointClass;
    }

    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
        this.pointClass = "Unknown";
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public String getPointClass()
    {
        return pointClass;
    }

    public double calculateDistance(Point point)
    {
        double distX = this.x - point.x;
        double distY = this.y - point.y;
        return Math.sqrt(distX*distX + distY*distY);
    }

    public void setPointClass(String pointClass)
    {
        this.pointClass = pointClass;
    }

}
