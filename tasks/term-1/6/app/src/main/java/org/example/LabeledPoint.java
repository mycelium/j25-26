package org.example;

public class LabeledPoint {
    public final double x;
    public final double y;
    public final String label; // класс

    public LabeledPoint(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    // Евклидово расстояние до другой точки
    public double distanceTo(double px, double py) {
        double dx = x - px;
        double dy = y - py;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
