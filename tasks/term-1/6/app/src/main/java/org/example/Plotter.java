package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Plotter {

    public static BufferedImage createPlot(List<Point> points, Point newPoint, String predictedClass) {
        int width = 1000, height = 1000;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        List<Point> allPoints = new ArrayList<>(points);
        if (newPoint != null) allPoints.add(newPoint);

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Point p : allPoints) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }
        double padding = 1.0;
        minX -= padding;
        maxX += padding;
        minY -= padding;
        maxY += padding;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        Color[] colors = {
                Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
                Color.MAGENTA, Color.CYAN, Color.PINK, Color.GRAY, Color.YELLOW, new Color(128, 0, 128)
        };
        String[] uniqueLabels = points.stream().map(Point::getLabel).distinct().toArray(String[]::new);

        for (Point p : points) {
            int idx = 0;
            for (int i = 0; i < uniqueLabels.length; i++) {
                if (uniqueLabels[i].equals(p.getLabel())) {
                    idx = i;
                    break;
                }
            }
            g.setColor(colors[idx % colors.length]);
            int x = (int) normalizeX(p.getX(), minX, maxX, width);
            int y = (int) (height - normalizeY(p.getY(), minY, maxY, height));
            g.fillOval(x - 4, y - 4, 8, 8);
        }

        g.setColor(Color.BLACK);
        int newX = (int) normalizeX(newPoint.getX(), minX, maxX, width);
        int newY = (int) (height - normalizeY(newPoint.getY(), minY, maxY, height));
        g.fillRect(newX - 6, newY - 6, 12, 12);

        g.setColor(Color.BLACK);
        g.drawString("Predicted class: " + predictedClass, 60, 40);

        int legendX = width - 200;
        int legendY = 60;
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Legend:", legendX, legendY - 10);
        for (int i = 0; i < uniqueLabels.length; i++) {
            g.setColor(colors[i % colors.length]);
            g.fillRect(legendX, legendY + i * 20, 15, 15);
            g.setColor(Color.BLACK);
            g.drawRect(legendX, legendY + i * 20, 15, 15);
            g.drawString(uniqueLabels[i], legendX + 25, legendY + 13 + i * 20);
        }

        g.dispose();
        return image;
    }

    private static double normalizeX(double x, double minX, double maxX, int width) {
        return 50 + (x - minX) / (maxX - minX) * (width - 100);
    }

    private static double normalizeY(double y, double minY, double maxY, int height) {
        return 50 + (y - minY) / (maxY - minY) * (height - 100);
    }

    public static void showPlot(BufferedImage image) {
        JFrame frame = new JFrame("KNN Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(image.getWidth(), image.getHeight());

        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);

        frame.setVisible(true);
    }
}
