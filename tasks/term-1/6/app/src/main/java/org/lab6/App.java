package org.lab6;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.List;

public class App {
    
    static class Point {
        double x;
        double y;
        String label;
        
        public Point(double x, double y, String label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }
        
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
            this.label = null;
        }
        

        public double distanceTo(Point other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
        
        @Override
        public String toString() {
            return String.format("Point(%.2f, %.2f, %s)", x, y, label);
        }
    }
    

    static class Neighbor implements Comparable<Neighbor> {
        double distance;
        String label;
        
        public Neighbor(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }
        
        @Override
        public int compareTo(Neighbor other) {
            return Double.compare(this.distance, other.distance);
        }
    }
    

    static class KNNClassifier {
        private List<Point> trainingData;
        private int k;
        
        public KNNClassifier(int k) {
            this.k = k;
            this.trainingData = new ArrayList<>();
        }
        
        public void addTrainingData(List<Point> points) {
            this.trainingData.addAll(points);
        }
        
        /**
         * 1. Считает расстояния от входной точки до всех тренировочных
         * 2. Сортирует по расстояниям 
         * 3. Берет к ближайших
         * 4. Считает количества ближайших по классам
         * 5. Возвращает класс с наибольшим кол-вом ближайших
         */
        public String classify(Point newPoint) {
            if (trainingData.isEmpty()) {
                throw new IllegalStateException("No training data available");
            }
            
            List<Neighbor> neighbors = new ArrayList<>();
            for (Point trainPoint : trainingData) {
                double distance = newPoint.distanceTo(trainPoint);
                neighbors.add(new Neighbor(distance, trainPoint.label));
            }
            
            Collections.sort(neighbors);
            List<Neighbor> kNearest = neighbors.subList(0, Math.min(k, neighbors.size()));
            
            Map<String, Integer> votes = new HashMap<>();
            for (Neighbor neighbor : kNearest) {
                votes.put(neighbor.label, votes.getOrDefault(neighbor.label, 0) + 1);
            }
            
            String bestLabel = null;
            int maxVotes = 0;
            for (Map.Entry<String, Integer> entry : votes.entrySet()) {
                if (entry.getValue() > maxVotes) {
                    maxVotes = entry.getValue();
                    bestLabel = entry.getKey();
                }
            }
            
            return bestLabel;
        }
        
        public List<Point> getTrainingData() {
            return trainingData;
        }
        
        public int getK() {
            return k;
        }
    }
    

    
    static class PointVisualizer {
        
        private Map<String, Color> colorMap;
        
        public PointVisualizer() {
            colorMap = new HashMap<>();
            colorMap.put("A", new Color(255, 99, 71));    // Red
            colorMap.put("B", new Color(60, 179, 113));   // Green
            colorMap.put("C", new Color(65, 105, 225));   // Blue
            colorMap.put("D", new Color(255, 165, 0));    // Orange
            colorMap.put("E", new Color(147, 112, 219));  // Purple
        }
        
        /**
         * Создает изображение и сохраняет в файл
         */
        public void visualize(List<Point> trainingPoints, Point testPoint, String filename) {
            XYSeriesCollection dataset = new XYSeriesCollection();
            
            Map<String, XYSeries> seriesMap = new HashMap<>();
            for (Point p : trainingPoints) {
                if (!seriesMap.containsKey(p.label)) {
                    seriesMap.put(p.label, new XYSeries("Class " + p.label));
                }
                seriesMap.get(p.label).add(p.x, p.y);
            }
            
            List<String> sortedClasses = new ArrayList<>(seriesMap.keySet());
            Collections.sort(sortedClasses);
            for (String label : sortedClasses) {
                dataset.addSeries(seriesMap.get(label));
            }
            
            if (testPoint != null && testPoint.label != null) {
                XYSeries testSeries = new XYSeries("Test Point (predicted: " + testPoint.label + ")");
                testSeries.add(testPoint.x, testPoint.y);
                dataset.addSeries(testSeries);
            }
            
            JFreeChart chart = ChartFactory.createScatterPlot(
                "KNN Classification Visualization",
                "X Coordinate",
                "Y Coordinate",
                dataset,
                PlotOrientation.VERTICAL,
                true,  // legend
                true,  // tooltips
                false  // urls
            );
            
            XYPlot plot = chart.getXYPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
            
            int seriesIndex = 0;
            for (String label : sortedClasses) {
                Color color = colorMap.getOrDefault(label, Color.BLACK);
                renderer.setSeriesPaint(seriesIndex, color);
                renderer.setSeriesShape(seriesIndex, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));
                seriesIndex++;
            }
            
            if (testPoint != null && testPoint.label != null) {
                Color testColor = colorMap.getOrDefault(testPoint.label, Color.BLACK);
                renderer.setSeriesPaint(seriesIndex, testColor);
                renderer.setSeriesShape(seriesIndex, new java.awt.geom.Rectangle2D.Double(-6, -6, 12, 12));
                renderer.setSeriesOutlinePaint(seriesIndex, Color.BLACK);
                renderer.setSeriesOutlineStroke(seriesIndex, new BasicStroke(2.0f));
            }
            
            plot.setRenderer(renderer);
            
            try {
                File outputFile = new File(filename);
                ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
                System.out.println("Visualization saved to: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving visualization: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Загрузка тренировочных данных из CSV
     * CSV формат: x,y,class
     */
    public static List<Point> loadTrainingData(String csvFilePath) throws IOException {
        List<Point> points = new ArrayList<>();
        
        try (Reader reader = new FileReader(csvFilePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            
            for (CSVRecord record : csvParser) {
                double x = Double.parseDouble(record.get("x"));
                double y = Double.parseDouble(record.get("y"));
                String label = record.get("class");
                points.add(new Point(x, y, label));
            }
        }
        
        return points;
    }
    
    public String getGreeting() {
        return "K-Nearest Neighbors (KNN) Classifier";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
        System.out.println("=".repeat(70));
        System.out.println();
        
        // Check command line arguments
        if (args.length < 2) {
            System.out.println("Usage: java App <x> <y>");
            System.out.println("  <x> - X coordinate of the point to classify");
            System.out.println("  <y> - Y coordinate of the point to classify");
            System.out.println("Example: java App 4.5 4.5");
            System.out.println("Running with default test point (4.5, 4.5)...");
            args = new String[]{"4.5", "4.5"};
        }
        
        double testX, testY;
        try {
            testX = Double.parseDouble(args[0]);
            testY = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid coordinates.");
            System.err.println("Example: java App 4.5 4.5");
            return;
        }
        
        Point testPoint = new Point(testX, testY);
        
        String csvPath = "training_data.csv";
        
        try {
            System.out.println("Loading training data from: " + csvPath);
            List<Point> trainingData = loadTrainingData(csvPath);
            System.out.println("Loaded " + trainingData.size() + " training points");
            
            Map<String, Integer> classCounts = new HashMap<>();
            for (Point p : trainingData) {
                classCounts.put(p.label, classCounts.getOrDefault(p.label, 0) + 1);
            }
            
            System.out.println("\nTraining data distribution:");
            List<String> sortedClasses = new ArrayList<>(classCounts.keySet());
            Collections.sort(sortedClasses);
            for (String label : sortedClasses) {
                System.out.println("Class " + label + ": " + classCounts.get(label) + " points");
            }
            System.out.println();
            
            int k = 5;
            System.out.println("Creating KNN classifier with k=" + k);
            KNNClassifier classifier = new KNNClassifier(k);
            classifier.addTrainingData(trainingData);
            System.out.println();
            
            System.out.println("INPUT: " + testX + ", " + testY);
            
            String predictedClass = classifier.classify(testPoint);
            testPoint.label = predictedClass;
            
            System.out.println("OUTPUT: Predicted class: " + predictedClass);
            
            System.out.println("Creating visualization...");
            PointVisualizer visualizer = new PointVisualizer();
            visualizer.visualize(trainingData, testPoint, "knn_classification.png");
            
        } catch (IOException e) {
            System.err.println("Error loading training data: " + e.getMessage());
            System.err.println("Please make sure the file '" + csvPath + "' exists in the project directory.");
            e.printStackTrace();
        }
    }
}
