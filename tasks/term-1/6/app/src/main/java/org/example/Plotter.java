package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Plotter {

    private static final int CANVAS_SIZE = 1000;
    private static final int BORDER = 50;

    private static final Color[] PALETTE = {
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
            Color.MAGENTA, Color.CYAN, Color.PINK, Color.GRAY,
            Color.YELLOW, new Color(128, 0, 128)
    };

    public static BufferedImage createPlot(List<Point> data, Point target, String label) {
        BufferedImage canvas = new BufferedImage(
                CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g2 = canvas.createGraphics();

        double[] bounds = findBounds(data, target);
        prepareBackground(g2);

        Map<String, Color> classColors = assignColors(data);

        drawDataset(g2, data, bounds, classColors);
        drawTargetPoint(g2, target, bounds);
        drawLegend(g2, classColors);
        drawTitle(g2, label);

        g2.dispose();
        return canvas;
    }

    private static void prepareBackground(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
    }

    private static double[] findBounds(List<Point> data, Point extra) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Point p : data) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        if (extra != null) {
            minX = Math.min(minX, extra.getX());
            maxX = Math.max(maxX, extra.getX());
            minY = Math.min(minY, extra.getY());
            maxY = Math.max(maxY, extra.getY());
        }

        return new double[]{
                minX - 1, maxX + 1,
                minY - 1, maxY + 1
        };
    }

    private static Map<String, Color> assignColors(List<Point> points) {
        Map<String, Color> colorMap = new HashMap<>();
        int index = 0;

        for (Point p : points) {
            if (!colorMap.containsKey(p.getLabel())) {
                colorMap.put(p.getLabel(), PALETTE[index % PALETTE.length]);
                index++;
            }
        }
        return colorMap;
    }

    private static void drawDataset(Graphics2D g, List<Point> points,
                                    double[] bounds, Map<String, Color> colors) {
        for (Point p : points) {
            g.setColor(colors.get(p.getLabel()));
            int x = scaleX(p.getX(), bounds);
            int y = scaleY(p.getY(), bounds);
            g.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    private static void drawTargetPoint(Graphics2D g, Point p, double[] bounds) {
        if (p == null) return;

        g.setColor(Color.BLACK);
        int x = scaleX(p.getX(), bounds);
        int y = scaleY(p.getY(), bounds);
        g.fillRect(x - 6, y - 6, 12, 12);
    }

    private static void drawLegend(Graphics2D g, Map<String, Color> legend) {
        int startX = CANVAS_SIZE - 200;
        int startY = 60;

        g.setColor(Color.BLACK);
        g.drawString("Legend:", startX, startY - 15);

        int i = 0;
        for (Map.Entry<String, Color> entry : legend.entrySet()) {
            g.setColor(entry.getValue());
            g.fillRect(startX, startY + i * 20, 15, 15);

            g.setColor(Color.BLACK);
            g.drawRect(startX, startY + i * 20, 15, 15);
            g.drawString(entry.getKey(), startX + 25, startY + 13 + i * 20);
            i++;
        }
    }

    private static void drawTitle(Graphics2D g, String label) {
        g.setColor(Color.BLACK);
        g.drawString("Predicted class: " + label, 60, 40);
    }

    private static int scaleX(double x, double[] b) {
        return BORDER + (int) ((x - b[0]) / (b[1] - b[0]) * (CANVAS_SIZE - 2 * BORDER));
    }

    private static int scaleY(double y, double[] b) {
        return CANVAS_SIZE - BORDER -
                (int) ((y - b[2]) / (b[3] - b[2]) * (CANVAS_SIZE - 2 * BORDER));
    }

    public static void showPlot(BufferedImage img) {
        JFrame window = new JFrame("KNN Visualization");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(new JLabel(new ImageIcon(img)));
        window.pack();
        window.setVisible(true);
    }
}
