package org.example.internal;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PointVisualizer {
    
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 50;
    private static final int POINT_RADIUS = 8;
    private static final int PREDICTED_POINT_RADIUS = 12;
    
    private static final Color[] CLASS_COLORS = {
        Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, 
        Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW
    };
    
    private List<Point> trainingPoints;
    private Point predictedPoint;
    private String predictedClass;
    
    public PointVisualizer(List<Point> trainingPoints) {
        this.trainingPoints = trainingPoints;
    }
    
    public void setPrediction(Point point, String predictedClass) {
        
        this.predictedPoint = point;
        this.predictedClass = predictedClass;
    }
    
    public void savePlotToFile(String filename) {
       
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        drawGrid(g2d);
        
        drawTrainingPoints(g2d);
        
        if (predictedPoint != null) {
            drawPredictedPoint(g2d);
        }
        
        drawLegend(g2d);
        
        g2d.dispose();
        
        try {
            
            ImageIO.write(image, "PNG", new File(filename));
            System.out.println("График сохранен в файл: " + filename);
        } catch (IOException e) {
            
            System.err.println("Ошибка сохранения графика: " + e.getMessage());
        }
    }
    
    private void drawGrid(Graphics2D g2d) {
        
        g2d.setColor(Color.LIGHT_GRAY);
        
        for (int x = MARGIN; x <= WIDTH - MARGIN; x += 50) {
            g2d.drawLine(x, MARGIN, x, HEIGHT - MARGIN);
        }
        
        for (int y = MARGIN; y <= HEIGHT - MARGIN; y += 50) {
            g2d.drawLine(MARGIN, y, WIDTH - MARGIN, y);
        }
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, WIDTH - MARGIN, HEIGHT - MARGIN); // X-axis
        g2d.drawLine(MARGIN, MARGIN, MARGIN, HEIGHT - MARGIN); // Y-axis
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("X", WIDTH - MARGIN + 5, HEIGHT - MARGIN - 5);
        g2d.drawString("Y", MARGIN - 15, MARGIN - 5);
    }
    
    private void drawTrainingPoints(Graphics2D g2d) {

        double minX = trainingPoints.stream().mapToDouble(Point::getX).min().orElse(0);
        double maxX = trainingPoints.stream().mapToDouble(Point::getX).max().orElse(1);
        double minY = trainingPoints.stream().mapToDouble(Point::getY).min().orElse(0);
        double maxY = trainingPoints.stream().mapToDouble(Point::getY).max().orElse(1);
        
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX -= xRange * 0.1;
        maxX += xRange * 0.1;
        minY -= yRange * 0.1;
        maxY += yRange * 0.1;
        
        for (Point point : trainingPoints) {
            
            int x = scaleX(point.getX(), minX, maxX);
            int y = scaleY(point.getY(), minY, maxY);
            Color color = getColorForClass(point.getLabel());
            
            g2d.setColor(color);
            g2d.fillOval(x - POINT_RADIUS/2, y - POINT_RADIUS/2, POINT_RADIUS, POINT_RADIUS);
            
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - POINT_RADIUS/2, y - POINT_RADIUS/2, POINT_RADIUS, POINT_RADIUS);
        }
    }
    
    private void drawPredictedPoint(Graphics2D g2d) {

        double minX = trainingPoints.stream().mapToDouble(Point::getX).min().orElse(predictedPoint.getX());
        double maxX = trainingPoints.stream().mapToDouble(Point::getX).max().orElse(predictedPoint.getX());
        double minY = trainingPoints.stream().mapToDouble(Point::getY).min().orElse(predictedPoint.getY());
        double maxY = trainingPoints.stream().mapToDouble(Point::getY).max().orElse(predictedPoint.getY());
        
        minX = Math.min(minX, predictedPoint.getX());
        maxX = Math.max(maxX, predictedPoint.getX());
        minY = Math.min(minY, predictedPoint.getY());
        maxY = Math.max(maxY, predictedPoint.getY());

        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX -= xRange * 0.1;
        maxX += xRange * 0.1;
        minY -= yRange * 0.1;
        maxY += yRange * 0.1;
        
        int x = scaleX(predictedPoint.getX(), minX, maxX);
        int y = scaleY(predictedPoint.getY(), minY, maxY);
        Color color = getColorForClass(predictedClass);
        
        g2d.setColor(color);
        g2d.fillOval(x - PREDICTED_POINT_RADIUS/2, y - PREDICTED_POINT_RADIUS/2, 
                     PREDICTED_POINT_RADIUS, PREDICTED_POINT_RADIUS);
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(x - PREDICTED_POINT_RADIUS/2, y - PREDICTED_POINT_RADIUS/2, 
                     PREDICTED_POINT_RADIUS, PREDICTED_POINT_RADIUS);
        
        g2d.drawLine(x - 4, y - 4, x + 4, y + 4);
        g2d.drawLine(x - 4, y + 4, x + 4, y - 4);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Предсказано: " + predictedClass, x + 15, y - 10);
    }
    
    private void drawLegend(Graphics2D g2d) {
        
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Легенда:", WIDTH - 150, 30);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int yPos = 50;
        
        List<String> uniqueClasses = trainingPoints.stream()
            .map(Point::getLabel)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        for (int i = 0; i < uniqueClasses.size(); i++) {
            String className = uniqueClasses.get(i);
            Color color = getColorForClass(className);
            
            g2d.setColor(color);
            g2d.fillRect(WIDTH - 150, yPos, 10, 10);
            
            g2d.setColor(Color.BLACK);
            g2d.drawRect(WIDTH - 150, yPos, 10, 10);
            g2d.drawString(className, WIDTH - 135, yPos + 10);
            
            yPos += 20;
        }
        
        if (predictedPoint != null) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(WIDTH - 150, yPos, 10, 10);
            g2d.drawLine(WIDTH - 145, yPos + 2, WIDTH - 140, yPos + 8);
            g2d.drawLine(WIDTH - 145, yPos + 8, WIDTH - 140, yPos + 2);
            g2d.drawString("Предсказанная", WIDTH - 135, yPos + 10);
        }
    }
    
    private int scaleX(double x, double minX, double maxX) {
        return MARGIN + (int)((x - minX) * (WIDTH - 2 * MARGIN) / (maxX - minX));
    }
    
    private int scaleY(double y, double minY, double maxY) {
        return HEIGHT - MARGIN - (int)((y - minY) * (HEIGHT - 2 * MARGIN) / (maxY - minY));
    }
    
    private Color getColorForClass(String className) {
        
        if (className == null) return Color.GRAY;
        
        int hash = className.hashCode();
        int index = Math.abs(hash) % CLASS_COLORS.length;
        
        return CLASS_COLORS[index];
    }
}
