package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class KNNVisualizer {
    private static final int IMAGE_SIZE = 600;
    private static final int PADDING = 60;
    private static final int POINT_RADIUS = 6;
    private static final int CROSS_SIZE = 10;
    private static final Color[] LABEL_COLORS = {
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.ORANGE,
            Color.MAGENTA,
            Color.CYAN,
            Color.PINK
    };
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color AXIS_COLOR = Color.BLACK;
    private final List<KNNClassificator.Point> trainingPoints;
    private final List<KNNClassificator.Point> testPoints;
    public KNNVisualizer(List<KNNClassificator.Point> trainingPoints,
                         List<KNNClassificator.Point> testPoints) {
        this.trainingPoints = trainingPoints;
        this.testPoints = testPoints;
    }
    public void visualizeAndSave(String filename) {
        if (trainingPoints.isEmpty() && testPoints.isEmpty()) {
            System.out.println("No points to visualize");
            return;
        }
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (KNNClassificator.Point point : trainingPoints) {
            minX = Math.min(minX, point.x());
            maxX = Math.max(maxX, point.x());
            minY = Math.min(minY, point.y());
            maxY = Math.max(maxY, point.y());
        }
        for (KNNClassificator.Point point : testPoints) {
            minX = Math.min(minX, point.x());
            maxX = Math.max(maxX, point.x());
            minY = Math.min(minY, point.y());
            maxY = Math.max(maxY, point.y());
        }
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX -= xRange * 0.1;
        maxX += xRange * 0.1;
        minY -= yRange * 0.1;
        maxY += yRange * 0.1;
        BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);
        drawAxes(g2d, minX, maxX, minY, maxY);
        for (KNNClassificator.Point point : trainingPoints) {
            if (point.label() != null) {
                drawTrainingPoint(g2d, point, minX, maxX, minY, maxY);
            }
        }
        for (KNNClassificator.Point point : testPoints) {
            if (point.label() != null) {
                drawTestPoint(g2d, point, minX, maxX, minY, maxY);
            }
        }
        try {
            File outputFile = new File(filename);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Visualization saved to: " +
                    outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving visualization: " + e.getMessage());
        }

        g2d.dispose();
    }
    private void drawAxes(Graphics2D g2d, double minX, double maxX,
                          double minY, double maxY) {
        g2d.setColor(AXIS_COLOR);
        g2d.setStroke(new BasicStroke(2));
        int plotWidth = IMAGE_SIZE - 2 * PADDING;
        int plotHeight = IMAGE_SIZE - 2 * PADDING;
        int xAxisY = IMAGE_SIZE - PADDING;
        g2d.drawLine(PADDING, xAxisY, IMAGE_SIZE - PADDING, xAxisY);
        int yAxisX = PADDING;
        g2d.drawLine(yAxisX, PADDING, yAxisX, IMAGE_SIZE - PADDING);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("X", IMAGE_SIZE - PADDING + 10, xAxisY + 5);
        g2d.drawString("Y", yAxisX - 20, PADDING - 10);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String minXLabel = String.format("%.1f", minX);
        g2d.drawString(minXLabel, PADDING - 15, xAxisY + 20);
        String maxXLabel = String.format("%.1f", maxX);
        g2d.drawString(maxXLabel, IMAGE_SIZE - PADDING - 15, xAxisY + 20);
        String minYLabel = String.format("%.1f", minY);
        g2d.drawString(minYLabel, yAxisX - 40, IMAGE_SIZE - PADDING + 5);
        String maxYLabel = String.format("%.1f", maxY);
        g2d.drawString(maxYLabel, yAxisX - 40, PADDING + 5);
    }
    private void drawTrainingPoint(Graphics2D g2d, KNNClassificator.Point point,
                                   double minX, double maxX, double minY, double maxY) {
        int plotWidth = IMAGE_SIZE - 2 * PADDING;
        int plotHeight = IMAGE_SIZE - 2 * PADDING;
        int x = PADDING + (int)((point.x() - minX) / (maxX - minX) * plotWidth);
        int y = IMAGE_SIZE - PADDING - (int)((point.y() - minY) / (maxY - minY) * plotHeight);
        Color color = getColorForLabel(point.label());
        g2d.setColor(color);
        g2d.fillOval(x - POINT_RADIUS, y - POINT_RADIUS,
                POINT_RADIUS * 2, POINT_RADIUS * 2);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(x - POINT_RADIUS, y - POINT_RADIUS,
                POINT_RADIUS * 2, POINT_RADIUS * 2);
    }
    private void drawTestPoint(Graphics2D g2d, KNNClassificator.Point point,
                               double minX, double maxX, double minY, double maxY) {
        int plotWidth = IMAGE_SIZE - 2 * PADDING;
        int plotHeight = IMAGE_SIZE - 2 * PADDING;

        int x = PADDING + (int)((point.x() - minX) / (maxX - minX) * plotWidth);
        int y = IMAGE_SIZE - PADDING - (int)((point.y() - minY) / (maxY - minY) * plotHeight);

        Color color = getColorForLabel(point.label());
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(3)); // Более толстый крест
        g2d.drawLine(x - CROSS_SIZE, y, x + CROSS_SIZE, y);
        g2d.drawLine(x, y - CROSS_SIZE, x, y + CROSS_SIZE);
    }
    private Color getColorForLabel(Integer label) {
        if (label == null || label < 0 || label >= LABEL_COLORS.length) {
            return Color.GRAY;
        }
        return LABEL_COLORS[label % LABEL_COLORS.length];
    }
}