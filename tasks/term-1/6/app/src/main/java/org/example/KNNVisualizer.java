package org.example;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class KNNVisualizer extends JPanel {
    private List<Point> trainingData;
    private List<Point> testPoints;
    private KNNClassifier classifier;
    
    // Colors for classes - matching your legend
    private static final Map<String, Color> CLASS_COLORS = Map.of(
        "A", new Color(255, 100, 100),    // Red
        "B", new Color(100, 100, 255),    // Blue  
        "C", new Color(100, 200, 100),    // Green
        "D", new Color(255, 200, 50)      // Orange
    );
    
    // Shapes for different point types
    private static final int TRAINING_POINT_SIZE = 8;
    private static final int TEST_POINT_SIZE = 10;
    
    public KNNVisualizer(List<Point> trainingData, List<Point> testPoints, int k) {
        this.trainingData = trainingData;
        this.testPoints = testPoints;
        this.classifier = new KNNClassifier(k);
        this.classifier.addTrainingPoints(trainingData);
        
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        
        // Classify all test points
        for (Point testPoint : testPoints) {
            String predictedLabel = classifier.classify(testPoint);
            testPoint.setLabel(predictedLabel);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawTrainingPoints(g2d);
        drawTestPoints(g2d);
        drawLegend(g2d);
        drawTitle(g2d);
    }
    
    private void drawTrainingPoints(Graphics2D g2d) {
        // Draw training points as circles
        for (Point point : trainingData) {
            Color color = CLASS_COLORS.get(point.getLabel());
            g2d.setColor(color);
            int x = (int) point.getX();
            int y = (int) point.getY();
            
            g2d.fillOval(x - TRAINING_POINT_SIZE/2, y - TRAINING_POINT_SIZE/2, 
                         TRAINING_POINT_SIZE, TRAINING_POINT_SIZE);
            
            // Black border
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - TRAINING_POINT_SIZE/2, y - TRAINING_POINT_SIZE/2, 
                         TRAINING_POINT_SIZE, TRAINING_POINT_SIZE);
        }
    }
    
    private void drawTestPoints(Graphics2D g2d) {
        // Draw test points as squares
        for (Point point : testPoints) {
            Color color = CLASS_COLORS.get(point.getLabel());
            g2d.setColor(color);
            int x = (int) point.getX();
            int y = (int) point.getY();
            
            g2d.fillRect(x - TEST_POINT_SIZE/2, y - TEST_POINT_SIZE/2, 
                        TEST_POINT_SIZE, TEST_POINT_SIZE);
            
            // Black border
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x - TEST_POINT_SIZE/2, y - TEST_POINT_SIZE/2, 
                        TEST_POINT_SIZE, TEST_POINT_SIZE);
            
            // Draw classification result near the point
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(point.getLabel(), x + 8, y - 8);
        }
    }
    
    private void drawLegend(Graphics2D g2d) {
        int legendX = 20;
        int legendY = getHeight() - 150;
        int boxSize = 15;
        int lineHeight = 25;
        
        // Legend background
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRect(legendX - 10, legendY - 20, 120, 130);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(legendX - 10, legendY - 20, 120, 130);
        
        // Legend title
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Legend", legendX, legendY);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Class A
        g2d.setColor(CLASS_COLORS.get("A"));
        g2d.fillOval(legendX, legendY + 20, boxSize, boxSize);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(legendX, legendY + 20, boxSize, boxSize);
        g2d.drawString("Class A", legendX + 25, legendY + 32);
        
        // Class B
        g2d.setColor(CLASS_COLORS.get("B"));
        g2d.fillOval(legendX, legendY + 45, boxSize, boxSize);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(legendX, legendY + 45, boxSize, boxSize);
        g2d.drawString("Class B", legendX + 25, legendY + 57);
        
        // Class C
        g2d.setColor(CLASS_COLORS.get("C"));
        g2d.fillOval(legendX, legendY + 70, boxSize, boxSize);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(legendX, legendY + 70, boxSize, boxSize);
        g2d.drawString("Class C", legendX + 25, legendY + 82);
        
        // Class D
        g2d.setColor(CLASS_COLORS.get("D"));
        g2d.fillOval(legendX, legendY + 95, boxSize, boxSize);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(legendX, legendY + 95, boxSize, boxSize);
        g2d.drawString("Class D", legendX + 25, legendY + 107);
    }
    
    private void drawTitle(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("K-Nearest Neighbors Classification", 250, 30);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Training points: circles | Test points: squares with labels", 250, 50);
    }
    
    public static void displayVisualization(List<Point> trainingData, List<Point> testPoints, int k) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("KNN Classification");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            KNNVisualizer visualizer = new KNNVisualizer(trainingData, testPoints, k);
            frame.add(visualizer);
            
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}