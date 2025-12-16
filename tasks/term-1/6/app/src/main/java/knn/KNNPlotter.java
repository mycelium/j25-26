package knn;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class KNNPlotter {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 50;
    private static final int POINT_RADIUS = 5;

    private static final Map<String, Color> CLASS_COLORS = new HashMap<>();
    static {
        CLASS_COLORS.put("ClassA", Color.RED);
        CLASS_COLORS.put("ClassB", Color.BLUE);
        CLASS_COLORS.put("ClassC", Color.GREEN);
        CLASS_COLORS.put("ClassD", Color.ORANGE);
        CLASS_COLORS.put("ClassE", Color.MAGENTA);
        CLASS_COLORS.put("Unknown", Color.BLACK);
    }

    public static void plotPoints(List<Point> points, String filename) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // границы
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Point p : points) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        // отступы к границам
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        final double finalMinX = minX - xRange * 0.1;
        final double finalMaxX = maxX + xRange * 0.1;
        final double finalMinY = minY - yRange * 0.1;
        final double finalMaxY = maxY + yRange * 0.1;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, WIDTH - MARGIN, HEIGHT - MARGIN);
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, MARGIN, MARGIN);

        g2d.setColor(Color.BLACK);
        g2d.drawString("X", WIDTH - MARGIN + 5, HEIGHT - MARGIN);
        g2d.drawString("Y", MARGIN, MARGIN - 5);

        for (Point p : points) {
            int x = scaleX(p.getX(), finalMinX, finalMaxX);
            int y = scaleY(p.getY(), finalMinY, finalMaxY);

            Color color = CLASS_COLORS.getOrDefault(p.getLabel(), Color.BLACK);

            if (p.isTestPoint()) {
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(x - POINT_RADIUS, y - POINT_RADIUS,
                        x + POINT_RADIUS, y + POINT_RADIUS);
                g2d.drawLine(x + POINT_RADIUS, y - POINT_RADIUS,
                        x - POINT_RADIUS, y + POINT_RADIUS);
            } else {
                g2d.setColor(color);
                g2d.fillOval(x - POINT_RADIUS, y - POINT_RADIUS,
                        POINT_RADIUS * 2, POINT_RADIUS * 2);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(x - POINT_RADIUS, y - POINT_RADIUS,
                        POINT_RADIUS * 2, POINT_RADIUS * 2);
            }
        }
        drawLegend(g2d, getUniqueLabels(points));

        g2d.dispose();

        try {
            File output = new File(filename);
            output.getParentFile().mkdirs();
            ImageIO.write(image, "png", output);
            System.out.println("Plot saved as: " + output.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving plot: " + e.getMessage());
        }
    }

    private static void drawLegend(Graphics2D g2d, Set<String> labels) {
        int legendX = WIDTH - 140;
        int legendY = 40;
        int lineHeight = 20;
        int padding = 8;
        int titleHeight = 20;

        List<String> sortedLabels = new ArrayList<>(labels);
        Collections.sort(sortedLabels);

        int legendWidth = 120;
        int legendHeight = sortedLabels.size() * lineHeight + padding * 2 + titleHeight;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(legendX - padding, legendY - padding, legendWidth, legendHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendX - padding, legendY - padding, legendWidth, legendHeight);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Legend", legendX, legendY + 5);

        g2d.drawLine(legendX - padding, legendY + titleHeight - 8,
                legendX - padding + legendWidth, legendY + titleHeight - 8);

        int i = 0;
        for (String label : sortedLabels) {
            Color color = CLASS_COLORS.getOrDefault(label, Color.BLACK);
            int itemY = legendY + titleHeight + i * lineHeight;

            g2d.setColor(color);
            g2d.fillOval(legendX, itemY - 6, 12, 12);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(legendX, itemY - 6, 12, 12);

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString(label, legendX + 18, itemY + 4);
            i++;
        }
    }

    private static int scaleX(double x, double minX, double maxX) {
        return (int)(MARGIN + (x - minX) * (WIDTH - 2 * MARGIN) / (maxX - minX));
    }

    private static int scaleY(double y, double minY, double maxY) {
        return (int)(HEIGHT - MARGIN - (y - minY) * (HEIGHT - 2 * MARGIN) / (maxY - minY));
    }

    private static Set<String> getUniqueLabels(List<Point> points) {
        return points.stream()
                .map(Point::getLabel)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
