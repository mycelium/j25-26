package org.example;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Generates PNG plots for point visualization
 */
public class PlotGenerator {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 50;
    private static final int POINT_SIZE = 8;

    /**
     * Creates a plot of training data points
     */
    public void createTrainingDataPlot(List<Point> trainingData, String filename) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw axes and grid
        drawGrid(g2d);

        // Draw training points
        for (Point point : trainingData) {
            drawPoint(g2d, point, DataGenerator.getClassColor(point.getLabel()), POINT_SIZE);
        }

        // Draw legend
        drawLegend(g2d, trainingData);

        g2d.dispose();
        saveImage(image, filename);
    }

    /**
     * Creates a plot showing training data and classified test points
     */
    public void createClassificationPlot(List<Point> trainingData, List<Point> testPoints,
                                       List<String> predictions, String filename) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw axes and grid
        drawGrid(g2d);

        // Draw training points (smaller)
        for (Point point : trainingData) {
            drawPoint(g2d, point, DataGenerator.getClassColor(point.getLabel()), POINT_SIZE / 2);
        }

        // Draw test points with classification results
        for (int i = 0; i < testPoints.size(); i++) {
            Point testPoint = testPoints.get(i);
            String prediction = predictions.get(i);
            Color color = DataGenerator.getClassColor(prediction);

            // Draw larger point with black border for test points
            drawPointWithBorder(g2d, testPoint, color, POINT_SIZE + 4);
        }

        // Draw legend
        drawLegend(g2d, trainingData);

        g2d.dispose();
        saveImage(image, filename);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.LIGHT_GRAY);

        // Draw grid lines
        for (int i = 0; i <= 12; i++) {
            int x = MARGIN + (int)((WIDTH - 2 * MARGIN) * i / 12.0);
            int y = MARGIN + (int)((HEIGHT - 2 * MARGIN) * i / 12.0);

            // Vertical lines
            g2d.drawLine(x, MARGIN, x, HEIGHT - MARGIN);
            // Horizontal lines
            g2d.drawLine(MARGIN, y, WIDTH - MARGIN, y);
        }

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        // X-axis
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, WIDTH - MARGIN, HEIGHT - MARGIN);
        // Y-axis
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, MARGIN, MARGIN);

        // Axis labels
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i <= 12; i += 2) {
            int x = MARGIN + (int)((WIDTH - 2 * MARGIN) * i / 12.0);
            int y = MARGIN + (int)((HEIGHT - 2 * MARGIN) * i / 12.0);

            // X-axis labels
            g2d.drawString(String.valueOf(i), x - 5, HEIGHT - MARGIN + 20);
            // Y-axis labels
            g2d.drawString(String.valueOf(12 - i), MARGIN - 25, y + 5);
        }
    }

    private void drawPoint(Graphics2D g2d, Point point, Color color, int size) {
        int x = (int) (MARGIN + (WIDTH - 2 * MARGIN) * point.getX() / 12.0);
        int y = (int) (HEIGHT - MARGIN - (HEIGHT - 2 * MARGIN) * point.getY() / 12.0);

        g2d.setColor(color);
        g2d.fillOval(x - size/2, y - size/2, size, size);
    }

    private void drawPointWithBorder(Graphics2D g2d, Point point, Color color, int size) {
        int x = (int) (MARGIN + (WIDTH - 2 * MARGIN) * point.getX() / 12.0);
        int y = (int) (HEIGHT - MARGIN - (HEIGHT - 2 * MARGIN) * point.getY() / 12.0);

        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x - size/2, y - size/2, size, size);

        // Draw inner color
        g2d.setColor(color);
        g2d.fillOval(x - (size-4)/2, y - (size-4)/2, size-4, size-4);
    }

    private void drawLegend(Graphics2D g2d, List<Point> trainingData) {
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        int legendX = WIDTH - 150;
        int legendY = MARGIN + 20;

        g2d.setColor(Color.BLACK);
        g2d.drawString("Classes:", legendX, legendY);

        // Get unique classes
        java.util.Set<String> classes = new java.util.HashSet<>();
        for (Point point : trainingData) {
            classes.add(point.getLabel());
        }

        int yOffset = legendY + 20;
        for (String label : classes) {
            g2d.setColor(DataGenerator.getClassColor(label));
            g2d.fillOval(legendX, yOffset - 5, 10, 10);

            g2d.setColor(Color.BLACK);
            g2d.drawString("Class " + label, legendX + 15, yOffset + 5);

            yOffset += 20;
        }

        // Add note about test points
        yOffset += 10;
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.drawString("Large points = test data", legendX - 20, yOffset);
    }

    private void saveImage(BufferedImage image, String filename) throws IOException {
        File outputFile = new File(filename);
        ImageIO.write(image, "png", outputFile);
        System.out.println("Plot saved to: " + outputFile.getAbsolutePath());
    }
}
