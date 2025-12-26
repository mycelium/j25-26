package org.example;

import java.util.List;

public record Bounds(double minX, double maxX, double minY, double maxY) {

    public static Bounds fromData(List<Point> points) {
        if (points == null || points.isEmpty()) {
            return new Bounds(0.0, 1.0, 0.0, 1.0);
        }

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Point p : points) {
            if (p == null) continue;
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        if (Double.isInfinite(minX) || Double.isInfinite(minY)) {
            return new Bounds(0.0, 1.0, 0.0, 1.0);
        }

        return new Bounds(minX, maxX, minY, maxY);
    }
}