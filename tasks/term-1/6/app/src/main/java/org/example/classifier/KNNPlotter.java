package org.example.classifier;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

public class KNNPlotter extends JPanel {
    private KNearestNeighbors classifier;
    private java.util.List<DataPoint> testPoints;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 50;
    private static final int LEGEND_WIDTH = 150;

    private Map<String, Color> colorMap;
    private Color[] colors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
            Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW,
            Color.DARK_GRAY, Color.LIGHT_GRAY
    };

    public KNNPlotter(KNearestNeighbors classifier, java.util.List<DataPoint> testPoints) {
        this.classifier = classifier;
        this.testPoints = testPoints;
        this.colorMap = new HashMap<>();
        initializeColorMap();

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
    }

    private void initializeColorMap() {
        Set<String> labels = new HashSet<>();

        for (DataPoint point : classifier.getTrainingData()) {
            labels.add(point.getLabel());
        }

        int colorIndex = 0;
        for (String label : labels) {
            colorMap.put(label, colors[colorIndex % colors.length]);
            colorIndex++;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2d);
        drawTrainingPoints(g2d);
        drawTestPoints(g2d);
        drawLegend(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.LIGHT_GRAY);

        for (int x = MARGIN; x <= WIDTH - MARGIN - LEGEND_WIDTH; x += 50) {
            g2d.drawLine(x, MARGIN, x, HEIGHT - MARGIN);
        }

        for (int y = MARGIN; y <= HEIGHT - MARGIN; y += 50) {
            g2d.drawLine(MARGIN, y, WIDTH - MARGIN - LEGEND_WIDTH, y);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, WIDTH - MARGIN - LEGEND_WIDTH, HEIGHT - MARGIN);
        g2d.drawLine(MARGIN, MARGIN, MARGIN, HEIGHT - MARGIN);

        g2d.drawString("X", WIDTH - MARGIN - LEGEND_WIDTH + 5, HEIGHT - MARGIN);
        g2d.drawString("Y", MARGIN, MARGIN - 10);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i <= 15; i++) {
            int x = scaleX(i);
            int y = scaleY(i);

            g2d.drawString(String.valueOf(i), x - 5, HEIGHT - MARGIN + 20);
            g2d.drawString(String.valueOf(i), MARGIN - 20, y + 5);
        }

        g2d.setColor(Color.GRAY);
        g2d.drawLine(WIDTH - MARGIN - LEGEND_WIDTH, MARGIN, WIDTH - MARGIN - LEGEND_WIDTH, HEIGHT - MARGIN);
    }

    private void drawTrainingPoints(Graphics2D g2d) {
        for (DataPoint point : classifier.getTrainingData()) {
            int x = scaleX(point.getX());
            int y = scaleY(point.getY());

            if (x <= WIDTH - MARGIN - LEGEND_WIDTH) {
                Color color = colorMap.get(point.getLabel());
                g2d.setColor(color);
                g2d.fillOval(x - 4, y - 4, 8, 8);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(x - 4, y - 4, 8, 8);
            }
        }
    }

    private void drawTestPoints(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));

        for (DataPoint point : testPoints) {
            int x = scaleX(point.getX());
            int y = scaleY(point.getY());

            if (x <= WIDTH - MARGIN - LEGEND_WIDTH) {
                String predictedLabel = classifier.classify(point);
                Color color = colorMap.get(predictedLabel);

                g2d.setColor(color);
                g2d.fillRect(x - 6, y - 6, 12, 12);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x - 6, y - 6, 12, 12);

                g2d.drawString(predictedLabel, x + 10, y - 10);
            }
        }
    }

    private void drawLegend(Graphics2D g2d) {
        int legendStartX = WIDTH - MARGIN - LEGEND_WIDTH + 20;
        int legendStartY = MARGIN + 40;

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Legend", legendStartX, legendStartY - 10);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        g2d.setColor(Color.BLACK);
        g2d.drawString("Training points:", legendStartX, legendStartY + 20);

        int yOffset = 40;
        for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
            g2d.setColor(entry.getValue());
            g2d.fillOval(legendStartX, legendStartY + yOffset, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(legendStartX, legendStartY + yOffset, 10, 10);

            g2d.drawString("Class " + entry.getKey(), legendStartX + 15, legendStartY + yOffset + 8);
            yOffset += 25;
        }

        yOffset += 10;
        g2d.setColor(Color.BLACK);
        g2d.drawString("Test points:", legendStartX, legendStartY + yOffset);

        yOffset += 20;
        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendStartX, legendStartY + yOffset, 10, 10);
        g2d.drawString("- test point", legendStartX + 15, legendStartY + yOffset + 8);

        yOffset += 30;
        g2d.setColor(Color.BLACK);
        g2d.drawString("Example:", legendStartX, legendStartY + yOffset);

        yOffset += 20;
        g2d.setColor(Color.RED);
        g2d.fillRect(legendStartX, legendStartY + yOffset, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendStartX, legendStartY + yOffset, 10, 10);
        g2d.drawString("- assigned to class A", legendStartX + 15, legendStartY + yOffset + 8);
    }

    private int scaleX(double x) {
        int fieldWidth = WIDTH - 2 * MARGIN - LEGEND_WIDTH;
        return MARGIN + (int) (x * fieldWidth / 15);
    }

    private int scaleY(double y) {
        return HEIGHT - MARGIN - (int) (y * (HEIGHT - 2 * MARGIN) / 15);
    }

    public void saveAsImage(String filename) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        paintComponent(g2d);
        g2d.dispose();

        try {
            ImageIO.write(image, "png", new File(filename));
            System.out.println("Graphic saved as: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving the image: " + e.getMessage());
        }
    }

    public void display() {
        JFrame frame = new JFrame("KNN Classification (0-15)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}