package org.example;

// Явные импорты — никаких *
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Visualization extends JPanel {
    private List<Point> trainingPoints;
    private List<Point> testPoints;
    private Map<String, Color> colorMap;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 100;
    private static final int TRAIN_RADIUS = 5;
    private static final int TEST_RADIUS = 8;

    public Visualization(List<Point> training, List<Point> test) {
        this.trainingPoints = training;
        this.testPoints = test;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        buildColorMap();
    }

    private void buildColorMap() {
        colorMap = new HashMap<>();
        Set<String> allLabels = new HashSet<>();

        for (Point p : trainingPoints) {
            if (p.getLabel() != null) allLabels.add(p.getLabel());
        }
        for (Point p : testPoints) {
            if (p.getLabel() != null) allLabels.add(p.getLabel());
        }

        Color[] palette = {
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
            Color.MAGENTA, Color.CYAN, Color.PINK, Color.DARK_GRAY, Color.YELLOW
        };

        List<String> sortedLabels = new ArrayList<>(allLabels);
        Collections.sort(sortedLabels);

        for (int i = 0; i < sortedLabels.size(); i++) {
            colorMap.put(sortedLabels.get(i), palette[i % palette.length]);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        double[] bounds = computeBounds();
        double minX = bounds[0], maxX = bounds[1], minY = bounds[2], maxY = bounds[3];

        double xRange = Math.max(1e-6, maxX - minX);
        double yRange = Math.max(1e-6, maxY - minY);
        minX -= xRange * 0.1;
        maxX += xRange * 0.1;
        minY -= yRange * 0.1;
        maxY += yRange * 0.1;

        double scaleX = (WIDTH - 2 * MARGIN) / (maxX - minX);
        double scaleY = (HEIGHT - 2 * MARGIN) / (maxY - minY);

        // обучающие
        for (Point p : trainingPoints) {
            if (p.getLabel() == null) continue;
            Color c = colorMap.get(p.getLabel());
            g2.setColor(c);
            int px = MARGIN + (int) ((p.getX() - minX) * scaleX);
            int py = HEIGHT - MARGIN - (int) ((p.getY() - minY) * scaleY);
            g2.fillOval(px - TRAIN_RADIUS, py - TRAIN_RADIUS, 2 * TRAIN_RADIUS, 2 * TRAIN_RADIUS);
        }

        // тестовые
        for (Point p : testPoints) {
            if (p.getLabel() == null) continue;
            Color c = colorMap.get(p.getLabel());
            g2.setColor(c);
            int px = MARGIN + (int) ((p.getX() - minX) * scaleX);
            int py = HEIGHT - MARGIN - (int) ((p.getY() - minY) * scaleY);
            g2.fillOval(px - TEST_RADIUS, py - TEST_RADIUS, 2 * TEST_RADIUS, 2 * TEST_RADIUS);
            g2.setColor(Color.BLACK);
            g2.drawOval(px - TEST_RADIUS, py - TEST_RADIUS, 2 * TEST_RADIUS, 2 * TEST_RADIUS);
        }

        drawLegend(g2);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("Классификация точек методом KNN", WIDTH / 2 - 120, 30);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Маленькие точки - обучающие, большие - тестовые", WIDTH / 2 - 160, 60);
    }

    private double[] computeBounds() {
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Point p : trainingPoints) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }
        for (Point p : testPoints) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        return new double[]{minX, maxX, minY, maxY};
    }

    private void drawLegend(Graphics2D g2) {
        int x0 = WIDTH - 120;
        int y0 = 50;
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("Классы:", x0, y0);

        List<String> labels = new ArrayList<>(colorMap.keySet());
        Collections.sort(labels);

        int y = y0 + 20;
        for (String label : labels) {
            Color c = colorMap.get(label);
            g2.setColor(c);
            g2.fillRect(x0, y, 14, 14);

            g2.setColor(Color.BLACK);
            g2.drawRect(x0, y, 14, 14);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString(label, x0 + 20, y + 11);
            y += 20;
        }
    }

    public static void show(List<Point> training, List<Point> test, String title) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new Visualization(training, test));
            frame.pack();
            frame.setSize(WIDTH, HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}