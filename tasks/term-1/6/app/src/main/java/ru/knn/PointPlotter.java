package ru.knn;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class PointPlotter {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    private static final int MARGIN = 50;
    private static final double PADDING_MULT = 0.1;
    private static final int POINT_SIZE = 12;

    private static final Map<String, Color> CLASS_COLORS = new HashMap<>();
    static {
        CLASS_COLORS.put("A", Color.RED);
        CLASS_COLORS.put("B", Color.BLUE);
        CLASS_COLORS.put("C", Color.GREEN);
        CLASS_COLORS.put("D", Color.ORANGE);
        CLASS_COLORS.put("E", Color.CYAN);
    }

    private static int scaleX(double x, double minX, double maxX) {
        return (int) (MARGIN + (x - minX) * (WIDTH - 2 * MARGIN) / (maxX - minX));
    }

    private static int scaleY(double y, double minY, double maxY) {
        return (int) (HEIGHT - MARGIN - (y - minY) * (HEIGHT - 2 * MARGIN) / (maxY - minY));
    }


    public static void plotPoints(List<Point> points, Point unknownPoint, String filename) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        double minX = points.stream().mapToDouble(Point::x).min().orElse(0);
        double maxX = points.stream().mapToDouble(Point::x).max().orElse(1);
        double minY = points.stream().mapToDouble(Point::y).min().orElse(0);
        double maxY = points.stream().mapToDouble(Point::y).max().orElse(1);

        if (unknownPoint != null) {
            minX = Math.min(minX, unknownPoint.x());
            maxX = Math.max(maxX, unknownPoint.x());
            minY = Math.min(minY, unknownPoint.y());
            maxY = Math.max(maxY, unknownPoint.y());
        }

        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX -= xRange * PADDING_MULT;
        maxX += xRange * PADDING_MULT;
        minY -= yRange * PADDING_MULT;
        maxY += yRange * PADDING_MULT;

        g2d.setColor(Color.BLACK);
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, WIDTH - MARGIN, HEIGHT - MARGIN); // X-axis
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, MARGIN, MARGIN); // Y-axis

        for (Point point : points) {
            Color color = CLASS_COLORS.getOrDefault(point.label(), Color.BLACK);
            g2d.setColor(color);
            int x = scaleX(point.x(), minX, maxX);
            int y = scaleY(point.y(), minY, maxY);
            g2d.fillOval(x - POINT_SIZE/2, y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);

            g2d.setColor(Color.BLACK);
            g2d.drawString(point.label(), x + POINT_SIZE, y - POINT_SIZE);
        }

        if (unknownPoint != null) {
            g2d.setColor(Color.BLACK);
            int x = scaleX(unknownPoint.x(), minX, maxX);
            int y = scaleY(unknownPoint.y(), minY, maxY);
            g2d.fillRect(x - POINT_SIZE/2, y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            g2d.drawString("X", x + POINT_SIZE, y - POINT_SIZE);
        }

        drawLegend(g2d, points);

        g2d.dispose();

        try {
            ImageIO.write(image, "png", new File("images/" + filename));
            System.out.println("График сохранен как: " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении графика: " + e.getMessage());
        }
    }

    private static void drawLegend(Graphics2D g2d, List<Point> points) {
        Set<String> labels = points.stream()
                .map(Point::label)
                .collect(Collectors.toSet());

        int legendX = WIDTH - 150;
        int legendY = 50;
        int lineHeight = 50;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(legendX - 10, legendY - 20, 140, labels.size() * lineHeight + 20);

        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendX - 10, legendY - 20, 140, labels.size() * lineHeight + 20);
        g2d.drawString("Легенда:", legendX, legendY);

        int i = 0;
        for (String label : labels) {
            Color color = CLASS_COLORS.getOrDefault(label, Color.BLACK);
            g2d.setColor(color);
            g2d.fillOval(legendX, legendY + 10 + i * lineHeight, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Класс " + label, legendX + 15, legendY + 20 + i * lineHeight);
            i++;
        }
    }
}