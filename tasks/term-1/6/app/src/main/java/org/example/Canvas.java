package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class Canvas extends JPanel {
    private List<Point> points;
    private List<Point> testPoints;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 50;
    private static final int POINT_SIZE = 8;
    
    private Map<String, Color> colorMap;
    
    public Canvas(List<Point> points, List<Point> testPoints) {
        this.points = points;
        this.testPoints = testPoints;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        initializeColorMap();
    }
    
    private void initializeColorMap() {
        colorMap = new HashMap<>();
        Color[] baseColors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW, Color.BLACK
        };
        
        Set<String> allLabels = new HashSet<>();
        for (Point p : points) {
            if (p.getLabel() != null) {
                allLabels.add(p.getLabel());
            }
        }
        for (Point p : testPoints) {
            if (p.getLabel() != null) {
                allLabels.add(p.getLabel());
            }
        }
        
        int colorIndex = 0;
        for (String label : allLabels) {
            colorMap.put(label, baseColors[colorIndex % baseColors.length]);
            colorIndex++;
        }
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        
        for (Point p : points) {
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
        
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX -= xRange * 0.1;
        maxX += xRange * 0.1;
        minY -= yRange * 0.1;
        maxY += yRange * 0.1;
        
        double scaleX = (WIDTH - 2 * MARGIN) / (maxX - minX);
        double scaleY = (HEIGHT - 2 * MARGIN) / (maxY - minY);
        
        // Тренировочная выборка
        for (Point p : points) {
            if (p.getLabel() != null) {
                g2d.setColor(colorMap.get(p.getLabel()));
                int x = MARGIN + (int) ((p.getX() - minX) * scaleX);
                int y = HEIGHT - MARGIN - (int) ((p.getY() - minY) * scaleY);
                g2d.fillOval(x - POINT_SIZE/2, y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            }
        }
        
        // Тестовая выборка
        for (Point p : testPoints) {
            if (p.getLabel() != null) {
                g2d.setColor(colorMap.get(p.getLabel()));
                int x = MARGIN + (int) ((p.getX() - minX) * scaleX);
                int y = HEIGHT - MARGIN - (int) ((p.getY() - minY) * scaleY);
                g2d.fillOval(x - POINT_SIZE, y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(x - POINT_SIZE, y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2);
            }
        }
        
        
        showLegend(g2d);
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("KNN Classification Visualization", WIDTH/2 - 100, 20);
    }

    private void showLegend(Graphics2D g2d) {
        int legendX = WIDTH - 80;
        int legendY = 30;
        int lineHeight = 20;
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Legend:", legendX, legendY);
        
        List<String> sortedLabels = new ArrayList<>(colorMap.keySet());
        Collections.sort(sortedLabels);
        
        int index = 0;
        for (String label : sortedLabels) {
            Color color = colorMap.get(label);
            g2d.setColor(color);
            g2d.fillRect(legendX, legendY + 10 + index * lineHeight, 10, 10);

            g2d.setColor(Color.BLACK);
            g2d.drawRect(legendX, legendY + 10 + index * lineHeight, 10, 10);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString(label, legendX + 15, legendY + 20 + index * lineHeight);
            index++;
        }
    }

    public static void displayVisualization(List<Point> points, List<Point> testPoints, String title) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            Canvas visualizer = new Canvas(points, testPoints);
            frame.add(visualizer);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void displayVisualization(List<Point> points, List<Point> testPoints) {
        displayVisualization(points, testPoints, "KNN Classification Visualization");
    }
}
