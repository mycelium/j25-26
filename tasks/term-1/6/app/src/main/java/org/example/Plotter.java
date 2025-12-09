package org.example;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class Plotter {
    
    public static void displayClassificationResults(DataSet trainingData, 
                                                   List<Point> testPoints) {
        List<Point> trainingPoints = trainingData.getPoints();
        createAndDisplayPlot(trainingPoints, testPoints, "KNN Classification Results");
    }
    
    private static void createAndDisplayPlot(List<Point> trainingPoints, 
                                           List<Point> testPoints, 
                                           String title) {
        
        BufferedImage plotImage = createPlotImage(trainingPoints, testPoints, title);
        
        
        displayImageInWindow(plotImage, title);
    }
    
    private static BufferedImage createPlotImage(List<Point> trainingPoints, 
                                               List<Point> testPoints, 
                                               String title) {
        int width = 850;  
        int height = 650; 
        int margin = 50;
        int trainingPointSize = 6;  
        int testPointSize = 10;     
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
       
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        
        
        for (Point p : trainingPoints) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }
        
        
        if (testPoints != null && !testPoints.isEmpty()) {
            for (Point p : testPoints) {
                if (p.getX() < minX) minX = p.getX();
                if (p.getX() > maxX) maxX = p.getX();
                if (p.getY() < minY) minY = p.getY();
                if (p.getY() > maxY) maxY = p.getY();
            }
        }
        
        
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX = minX - xRange * 0.1;
        maxX = maxX + xRange * 0.1;
        minY = minY - yRange * 0.1;
        maxY = maxY + yRange * 0.1;
        
        
        double scaleX = (width - 2 * margin - 120) / (maxX - minX); 
        double scaleY = (height - 2 * margin) / (maxY - minY);
        
        
        drawGridAndAxes(g2d, width, height, margin, minX, maxX, minY, maxY, scaleX, scaleY);
        
     
        Map<String, Color> colorMap = createColorMap(trainingPoints, testPoints);
        
        
        drawTrainingPoints(g2d, trainingPoints, colorMap, minX, minY, scaleX, scaleY, 
                          width, height, margin, trainingPointSize);
        
        
        if (testPoints != null && !testPoints.isEmpty()) {
            drawTestPoints(g2d, testPoints, colorMap, minX, minY, scaleX, scaleY, 
                          width, height, margin, testPointSize);
        }
        
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 30);
        
        
        drawLegend(g2d, colorMap, testPoints != null && !testPoints.isEmpty(), width, height);
        
        g2d.dispose();
        return image;
    }
    
    private static void drawGridAndAxes(Graphics2D g2d, int width, int height, int margin,
                                       double minX, double maxX, double minY, double maxY,
                                       double scaleX, double scaleY) {
       
        g2d.setColor(new Color(240, 240, 240));
        
        
        for (int i = 0; i <= 10; i++) {
            double x = minX + i * (maxX - minX) / 10;
            int screenX = margin + (int)((x - minX) * scaleX);
            g2d.drawLine(screenX, margin, screenX, height - margin);
        }
        
        
        for (int i = 0; i <= 10; i++) {
            double y = minY + i * (maxY - minY) / 10;
            int screenY = height - margin - (int)((y - minY) * scaleY);
            g2d.drawLine(margin, screenY, width - margin - 120, screenY); 
        }
        
        
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        
        
        int xAxisY = height - margin;
        g2d.drawLine(margin, xAxisY, width - margin - 120, xAxisY);
        
        
        g2d.drawLine(margin, margin, margin, height - margin);
        
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        
        
        for (int i = 0; i <= 10; i++) {
            double x = minX + i * (maxX - minX) / 10;
            int screenX = margin + (int)((x - minX) * scaleX);
            String label = String.format("%.1f", x);
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.drawString(label, screenX - labelWidth / 2, xAxisY + 20);
        }
        
        
        for (int i = 0; i <= 10; i++) {
            double y = minY + i * (maxY - minY) / 10;
            int screenY = height - margin - (int)((y - minY) * scaleY);
            String label = String.format("%.1f", y);
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.drawString(label, margin - labelWidth - 5, screenY + 5);
        }
        
        
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("X", width - margin - 120 + 10, xAxisY); 
        g2d.drawString("Y", margin, margin - 10);
    }
    
    private static Map<String, Color> createColorMap(List<Point> trainingPoints, List<Point> testPoints) {
        Map<String, Color> colorMap = new HashMap<>();
        Color[] colors = {
            Color.RED, Color.BLUE, Color.GREEN, 
            Color.ORANGE, Color.MAGENTA, Color.CYAN,
            new Color(128, 0, 128), 
            new Color(165, 42, 42),  
            Color.PINK, Color.YELLOW, Color.GRAY
        };
        
        Set<String> labels = new HashSet<>();
        
        
        for (Point p : trainingPoints) {
            String label = p.getLabel();
            if (label != null && !label.isEmpty()) {
                labels.add(label);
            }
        }
        
        if (testPoints != null) {
            for (Point p : testPoints) {
                String label = p.getLabel();
                if (label != null && !label.isEmpty()) {
                    String[] parts = label.split(":");
                    if (parts.length > 1) {
                        labels.add(parts[1].trim());
                    }
                }
            }
        }
        
  
        List<String> sortedLabels = new ArrayList<>(labels);
        Collections.sort(sortedLabels);
        
        for (int i = 0; i < sortedLabels.size(); i++) {
            colorMap.put(sortedLabels.get(i), colors[i % colors.length]);
        }
        
        return colorMap;
    }
    
    private static void drawTrainingPoints(Graphics2D g2d, List<Point> trainingPoints, 
                                          Map<String, Color> colorMap,
                                          double minX, double minY, 
                                          double scaleX, double scaleY,
                                          int width, int height, int margin,
                                          int pointSize) {
        
        for (Point p : trainingPoints) {
            String label = p.getLabel();
            if (label == null || label.isEmpty()) {
                continue;
            }
            
            int screenX = margin + (int)((p.getX() - minX) * scaleX);
            int screenY = height - margin - (int)((p.getY() - minY) * scaleY);
            
            Color pointColor = colorMap.get(label);
            if (pointColor == null) {
                pointColor = Color.LIGHT_GRAY;
            }
            
            g2d.setColor(pointColor);
            g2d.fillOval(screenX - pointSize/2, screenY - pointSize/2, 
                       pointSize, pointSize);
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawOval(screenX - pointSize/2, screenY - pointSize/2, 
                       pointSize, pointSize);
        }
    }
    
    private static void drawTestPoints(Graphics2D g2d, List<Point> testPoints, 
                                      Map<String, Color> colorMap,
                                      double minX, double minY, 
                                      double scaleX, double scaleY,
                                      int width, int height, int margin,
                                      int pointSize) {
        
        for (int i = 0; i < testPoints.size(); i++) {
            Point p = testPoints.get(i);
            String label = p.getLabel();
            if (label == null || label.isEmpty()) {
                continue;
            }
            
    
            int screenX = margin + (int)((p.getX() - minX) * scaleX);
            int screenY = height - margin - (int)((p.getY() - minY) * scaleY);
            
         
            Color pointColor = Color.BLACK; 
            if (label.contains(":")) {
                String[] parts = label.split(":");
                if (parts.length > 1) {
                    pointColor = colorMap.get(parts[1].trim());
                }
            }
            
            if (pointColor == null) {
                pointColor = Color.BLACK;
            }
            
         
            g2d.setColor(new Color(pointColor.getRed(), pointColor.getGreen(), 
                                 pointColor.getBlue(), 200));
            g2d.fillOval(screenX - pointSize/2, screenY - pointSize/2, 
                       pointSize, pointSize);
            
       
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(screenX - pointSize/2, screenY - pointSize/2, 
                       pointSize, pointSize);
            

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String testNumber = "T" + (i+1);
            g2d.drawString(testNumber, screenX + pointSize/2 + 5, screenY);
        }
    }
    
    private static void drawLegend(Graphics2D g2d, Map<String, Color> colorMap, 
                                  boolean hasTestPoints, int width, int height) {
        
        int legendX = width - 200;  
        int legendY = 50;
        int boxHeight = 22;  
        int itemsCount = colorMap.size() + (hasTestPoints ? 1 : 0);
        

        int legendHeight = itemsCount * boxHeight + 25;
        

        g2d.setColor(new Color(255, 255, 255, 240));  
        g2d.fillRect(legendX - 10, legendY - 10, 180, legendHeight);
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(legendX - 10, legendY - 10, 180, legendHeight);
        
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Legend:", legendX, legendY);
        
      
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        List<String> sortedLabels = new ArrayList<>(colorMap.keySet());
        Collections.sort(sortedLabels);
        
        for (int i = 0; i < sortedLabels.size(); i++) {
            String label = sortedLabels.get(i);
            Color color = colorMap.get(label);
            
          
            g2d.setColor(color);
            g2d.fillRect(legendX, legendY + 20 + i * boxHeight, 15, 15);
            
           
            g2d.setColor(Color.BLACK);
            g2d.drawRect(legendX, legendY + 20 + i * boxHeight, 15, 15);
            
           
            g2d.drawString(label, legendX + 20, legendY + 32 + i * boxHeight);
        }
        
       
        if (hasTestPoints) {
            int offset = sortedLabels.size();
            
           
            g2d.setColor(new Color(0, 0, 0, 100)); 
            g2d.fillOval(legendX, legendY + 20 + offset * boxHeight, 15, 15);
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(legendX, legendY + 20 + offset * boxHeight, 15, 15);
            
            
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Test Points", legendX + 20, legendY + 32 + offset * boxHeight);
            
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString("(colored by predicted class)", legendX, legendY + 47 + offset * boxHeight);
        }
    }
    
    private static void displayImageInWindow(BufferedImage image, String title) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            JLabel label = new JLabel(new ImageIcon(image));
            frame.getContentPane().add(label);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}