package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataVisualizer {
    private static final int IMAGE_SIZE = 800;
    private static final int PADDING = 50;
    private static final int POINT_SIZE = 10;
    
    //цвета для классов
    private static final Color[] CLASS_COLORS = {
        new Color(255, 0, 0),    // Красный
        new Color(0, 255, 0),    // Зеленый
        new Color(0, 0, 255),    // Синий
        new Color(255, 255, 0),  // Желтый
        new Color(255, 0, 255),  // Пурпурный
        new Color(0, 255, 255)   // Голубой
    };
    
    //тестовые
    private static final Color TEST_POINT_COLOR = Color.BLACK;

    public static void visualizePoints(List<Point> points, Point testPoint, String outputPath) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        //границы
        for (Point p : points) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }
        
        if (testPoint != null) {
            minX = Math.min(minX, testPoint.getX());
            maxX = Math.max(maxX, testPoint.getX());
            minY = Math.min(minY, testPoint.getY());
            maxY = Math.max(maxY, testPoint.getY());
        }
        
        //отступы
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX = Math.min(minX - xRange * 0.1, 0);
        maxX = Math.max(maxX + xRange * 0.1, 0);
        minY = Math.min(minY - yRange * 0.1, 0);
        maxY = Math.max(maxY + yRange * 0.1, 0);
        
        //пустое изображение
        BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);
        
        //сглаживание
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawAxes(g2d, minX, maxX, minY, maxY);
        
        //выборки точки
        for (Point p : points) {
            int colorIndex = getColorIndex(p.getLabel());
            g2d.setColor(CLASS_COLORS[colorIndex % CLASS_COLORS.length]);
            
            int x = scaleX(p.getX(), minX, maxX);
            int y = scaleY(p.getY(), minY, maxY);
            
            g2d.fillOval(x - POINT_SIZE/2, y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - POINT_SIZE/2, y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
        }
        
        //тестовуая точка
        if (testPoint != null) {
            g2d.setColor(TEST_POINT_COLOR);
            int x = scaleX(testPoint.getX(), minX, maxX);
            int y = scaleY(testPoint.getY(), minY, maxY);
            
            //крестик
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x - POINT_SIZE, y - POINT_SIZE, x + POINT_SIZE, y + POINT_SIZE);
            g2d.drawLine(x + POINT_SIZE, y - POINT_SIZE, x - POINT_SIZE, y + POINT_SIZE);
            
            if (testPoint.getLabel() != null) {
                g2d.drawString("Test: " + testPoint.getLabel(), x + 15, y - 15);
            }
        }
        
        //легенда
        drawLegend(g2d, points);
        
        g2d.dispose();
        
        //сохранение
        try {
            ImageIO.write(image, "png", new File(outputPath));
            System.out.println("Graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }
    
    private static void drawAxes(Graphics2D g2d, double minX, double maxX, double minY, double maxY) {
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));
        
        //X
        int xAxisY = scaleY(0, minY, maxY);
        g2d.drawLine(PADDING, xAxisY, IMAGE_SIZE - PADDING, xAxisY);
        
        //Y
        int yAxisX = scaleX(0, minX, maxX);
        g2d.drawLine(yAxisX, PADDING, yAxisX, IMAGE_SIZE - PADDING);
        
        g2d.setColor(Color.BLACK);
        g2d.drawString("X", IMAGE_SIZE - PADDING + 5, xAxisY - 5);
        g2d.drawString("Y", yAxisX + 5, PADDING - 5);
    }
    
    private static void drawLegend(Graphics2D g2d, List<Point> points) {
        Set<String> labels = new HashSet<>();
        for (Point p : points) {
            labels.add(p.getLabel());
        }
        
        int x = IMAGE_SIZE - 150;
        int y = 30;
        int lineHeight = 20;
        
        g2d.setColor(Color.BLACK);
        g2d.drawString("Legend:", x, y);
        
        int i = 0;
        for (String label : labels) {
            int colorIndex = getColorIndex(label);
            g2d.setColor(CLASS_COLORS[colorIndex % CLASS_COLORS.length]);
            g2d.fillRect(x, y + (i+1) * lineHeight, 10, 10);
            
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, x + 15, y + (i+1) * lineHeight + 9);
            i++;
        }
    }
    
    private static int scaleX(double x, double minX, double maxX) {
        return (int) (PADDING + (x - minX) * (IMAGE_SIZE - 2 * PADDING) / (maxX - minX));
    }
    
    private static int scaleY(double y, double minY, double maxY) {
        return (int) (IMAGE_SIZE - PADDING - (y - minY) * (IMAGE_SIZE - 2 * PADDING) / (maxY - minY));
    }
    
    private static int getColorIndex(String label) {
        if (label == null) return 0;
        return Math.abs(label.hashCode()) % CLASS_COLORS.length;
    }
}