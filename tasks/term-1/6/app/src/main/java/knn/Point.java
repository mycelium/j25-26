package knn;

public class Point {
    private double x;
    private double y;
    private String label;
    private boolean isTestPoint;

    public Point(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.isTestPoint = false;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.label = null;
        this.isTestPoint = true;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getLabel() { return label; }
    public boolean isTestPoint() { return isTestPoint; }

    public void setLabel(String label) {
        this.label = label;
    }

    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f) - %s", x, y, isTestPoint ? "Unknown" : label);
    }
}
