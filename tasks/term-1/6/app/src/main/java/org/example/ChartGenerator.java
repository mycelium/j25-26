package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

public class ChartGenerator {
    
    public static void generateChart(KNNClassifier classifier, Point testPoint, String outputPath) {
        try {
            
            BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 800, 600);
            
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            drawGrid(g);
            
            drawTrainingPoints(g, classifier.getTrainingData());
            
            if (testPoint != null) {
                drawTestPoint(g, testPoint);
            }
            
            drawLegend(g);
            
            ImageIO.write(image, "png", new File(outputPath));
            System.out.println("Chart saved to: " + outputPath);
            
            g.dispose();
            
        } catch (Exception e) {
            System.err.println("Error creating chart: " + e.getMessage());
        }
    }

    public static void generateTrainingDataChart(List<Point> trainingData, String outputPath) {
        try {
            BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 800, 600);
            
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            drawGrid(g);
            
            drawTrainingPoints(g, trainingData);
            
            drawLegend(g);
            
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Training Data Distribution", 300, 30);
            
            ImageIO.write(image, "png", new File(outputPath));
            System.out.println("Training data chart saved to: " + outputPath);
            
            g.dispose();
            
        } catch (Exception e) {
            System.err.println("Error creating training chart: " + e.getMessage());
        }
    }
    
    private static void drawGrid(Graphics2D g) {
        g.setColor(Color.LIGHT_GRAY);
        
        for (int x = 50; x <= 750; x += 50) {
            g.drawLine(x, 50, x, 550);
        }
        
        for (int y = 50; y <= 550; y += 50) {
            g.drawLine(50, y, 750, y);
        }
        
        g.setColor(Color.BLACK);
        g.drawLine(50, 550, 750, 550); 
        g.drawLine(50, 50, 50, 550);   
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("X", 760, 545);
        g.drawString("Y", 35, 40);
        
        for (int i = 0; i <= 10; i++) {
            int x = 50 + i * 70;
            int y = 550 - i * 50;
            
            g.drawString(String.valueOf(i), x - 5, 570);
            g.drawString(String.valueOf(i), 25, y + 5);
        }
    }
    
    private static void drawTrainingPoints(Graphics2D g, List<Point> trainingData) {
        for (Point point : trainingData) {
            int x = (int)(point.getX() * 70 + 50);
            int y = 550 - (int)(point.getY() * 50);
            
            switch(point.getLabel()) {
                case "A":
                    g.setColor(Color.RED);
                    g.fillOval(x - 4, y - 4, 8, 8);
                    break;
                case "B":
                    g.setColor(Color.BLUE);
                    g.fillOval(x - 4, y - 4, 8, 8);
                    break;
                case "C":
                    g.setColor(Color.GREEN);
                    g.fillOval(x - 4, y - 4, 8, 8);
                    break;
            }
            
            g.setColor(Color.BLACK);
            g.drawOval(x - 4, y - 4, 8, 8);
        }
    }
    
    private static void drawTestPoint(Graphics2D g, Point testPoint) {
        int x = (int)(testPoint.getX() * 70 + 50);
        int y = 550 - (int)(testPoint.getY() * 50);
        
        g.setColor(Color.BLACK);
        g.fillRect(x - 5, y - 5, 10, 10);
        
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Test: " + testPoint.getLabel(), x + 15, y - 10);
    }
    
    private static void drawLegend(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Legend:", 600, 80);
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        
        g.setColor(Color.RED);
        g.fillOval(600, 100, 8, 8);
        g.setColor(Color.BLACK);
        g.drawString("Class A", 615, 108);
        
        g.setColor(Color.BLUE);
        g.fillOval(600, 120, 8, 8);
        g.setColor(Color.BLACK);
        g.drawString("Class B", 615, 128);
        
        g.setColor(Color.GREEN);
        g.fillOval(600, 140, 8, 8);
        g.setColor(Color.BLACK);
        g.drawString("Class C", 615, 148);
        
        g.setColor(Color.BLACK);
        g.fillRect(600, 160, 8, 8);
        g.drawString("Test Point", 615, 168);
    }
    
    public static void showChartInWindow(KNNClassifier classifier, Point testPoint, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                drawGrid(g2d);
                drawTrainingPoints(g2d, classifier.getTrainingData());
                if (testPoint != null) {
                    drawTestPoint(g2d, testPoint);
                }
                drawLegend(g2d);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 600);
            }
        };
        
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
