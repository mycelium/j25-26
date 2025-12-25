package org.example;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class DataVisualizer {
    private static final int IMAGE_SIZE = 800;
    private static final int PADDING = 60;
    private static final int POINT_RADIUS = 8;
    private static final int TEST_POINT_SIZE = 12;
    
    private static final Color[] CLASS_COLORS = {
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.MAGENTA,
        Color.ORANGE,
        Color.CYAN
    };
    
    private static final Color TEST_POINT_COLOR = Color.BLACK;
    private static final Color AXIS_COLOR = Color.GRAY;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color GRID_COLOR = new Color(240, 240, 240);

    public static void visualizePoints(List<Point> trainingPoints, Point testPoint, String outputPath) {
        if (trainingPoints.isEmpty() && testPoint == null) {
            throw new IllegalArgumentException("No points to visualize");
        }

        List<Point> allPoints = new ArrayList<>(trainingPoints);
        if (testPoint != null) {
            allPoints.add(testPoint);
        }

        double minX = allPoints.stream().mapToDouble(Point::getX).min().orElse(0);
        double maxX = allPoints.stream().mapToDouble(Point::getX).max().orElse(1);
        double minY = allPoints.stream().mapToDouble(Point::getY).min().orElse(0);
        double maxY = allPoints.stream().mapToDouble(Point::getY).max().orElse(1);
        
        double paddingX = (maxX - minX) * 0.1;
        double paddingY = (maxY - minY) * 0.1;
        minX -= paddingX;
        maxX += paddingX;
        minY -= paddingY;
        maxY += paddingY;

        BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);

        drawGrid(g, minX, maxX, minY, maxY);
        
        drawAxes(g, minX, maxX, minY, maxY);

        Map<String, Color> colorMap = new HashMap<>();
        int colorIndex = 0;
        
        for (Point point : trainingPoints) {
            String label = point.getLabel();
            if (!colorMap.containsKey(label)) {
                colorMap.put(label, CLASS_COLORS[colorIndex % CLASS_COLORS.length]);
                colorIndex++;
            }
            
            Color pointColor = colorMap.get(label);
            drawPoint(g, point, pointColor, POINT_RADIUS, false, minX, maxX, minY, maxY);
        }

        if (testPoint != null) {
            drawPoint(g, testPoint, TEST_POINT_COLOR, TEST_POINT_SIZE, true, minX, maxX, minY, maxY);
            if (testPoint.getLabel() != null) {
                int x = scaleX(testPoint.getX(), minX, maxX);
                int y = scaleY(testPoint.getY(), minY, maxY);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString("Test: " + testPoint.getLabel(), x + 15, y - 15);
            }
        }

        drawLegend(g, colorMap);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("X", IMAGE_SIZE - PADDING + 10, 
                    scaleY(0, minY, maxY) - 5);
        g.drawString("Y", scaleX(0, minX, maxX) + 10, PADDING - 10);

        g.dispose();

        try {
            File outputFile = new File(outputPath);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Graph saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }

    private static void drawGrid(Graphics2D g, double minX, double maxX, double minY, double maxY) {
        g.setColor(GRID_COLOR);
        g.setStroke(new BasicStroke(1));

        double stepX = (maxX - minX) / 10;
        for (double x = minX; x <= maxX; x += stepX) {
            int screenX = scaleX(x, minX, maxX);
            g.drawLine(screenX, PADDING, screenX, IMAGE_SIZE - PADDING);
        }

        double stepY = (maxY - minY) / 10;
        for (double y = minY; y <= maxY; y += stepY) {
            int screenY = scaleY(y, minY, maxY);
            g.drawLine(PADDING, screenY, IMAGE_SIZE - PADDING, screenY);
        }
    }

    private static void drawAxes(Graphics2D g, double minX, double maxX, double minY, double maxY) {
        g.setColor(AXIS_COLOR);
        g.setStroke(new BasicStroke(2));

        int xAxisY = scaleY(0, minY, maxY);
        g.drawLine(PADDING, xAxisY, IMAGE_SIZE - PADDING, xAxisY);
        
        int yAxisX = scaleX(0, minX, maxX);
        g.drawLine(yAxisX, PADDING, yAxisX, IMAGE_SIZE - PADDING);

        drawArrow(g, IMAGE_SIZE - PADDING, xAxisY, 10, 0); 
        drawArrow(g, yAxisX, PADDING, 10, 90); 
    }

    private static void drawArrow(Graphics2D g, int x, int y, int size, int direction) {
        int[] xPoints, yPoints;
        if (direction == 0) { // стрелка вправо
            xPoints = new int[]{x, x - size, x - size};
            yPoints = new int[]{y, y - size/2, y + size/2};
        } else { // стрелка вверх
            xPoints = new int[]{x, x - size/2, x + size/2};
            yPoints = new int[]{y, y + size, y + size};
        }
        g.fillPolygon(xPoints, yPoints, 3);
    }

    private static void drawPoint(Graphics2D g, Point point, Color color, int size, boolean isTestPoint,
                                 double minX, double maxX, double minY, double maxY) {
        int x = scaleX(point.getX(), minX, maxX);
        int y = scaleY(point.getY(), minY, maxY);

        g.setColor(color);
        
        if (isTestPoint) {
            g.setStroke(new BasicStroke(3));
            g.drawLine(x - size, y - size, x + size, y + size);
            g.drawLine(x + size, y - size, x - size, y + size);
        } else {
            g.fillOval(x - size/2, y - size/2, size, size);
            g.setColor(Color.BLACK);
            g.drawOval(x - size/2, y - size/2, size, size);
        }
    }

    private static void drawLegend(Graphics2D g, Map<String, Color> colorMap) {
        int x = IMAGE_SIZE - 180;
        int y = 40;
        int boxSize = 15;
        int lineHeight = 25;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Legend:", x, y);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        
        int i = 0;
        for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
            g.setColor(entry.getValue());
            g.fillRect(x, y + (i + 1) * lineHeight, boxSize, boxSize);
            
            g.setColor(Color.BLACK);
            g.drawRect(x, y + (i + 1) * lineHeight, boxSize, boxSize);
            g.drawString(entry.getKey(), x + boxSize + 10, y + (i + 1) * lineHeight + 12);
            i++;
        }
    }

    private static int scaleX(double x, double minX, double maxX) {
        return (int)(PADDING + (x - minX) * (IMAGE_SIZE - 2 * PADDING) / (maxX - minX));
    }

    private static int scaleY(double y, double minY, double maxY) {
        return (int)(IMAGE_SIZE - PADDING - (y - minY) * (IMAGE_SIZE - 2 * PADDING) / (maxY - minY));
    }
}