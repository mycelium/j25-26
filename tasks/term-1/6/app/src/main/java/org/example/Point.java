package org.example;

public record Point(double x, double y, String label) {

    public Point(double x, double y) {
        this(x, y, null);
    }

    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f) - %s", x, y, label);
    }
}