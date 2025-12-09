package org.example;

import java.util.ArrayList;
import java.util.List;

public class App {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("    K-Nearest Neighbors Classifier");
        System.out.println("========================================");
        
        try {
          
            System.out.println("\n=== Dataset 1 ===");
            DataSet trainingData1 = DataSet.getFixedDataSet1();
            trainAndTest(trainingData1, 1, 5); 
            
            
            Thread.sleep(2000);
            
            
            System.out.println("\n\n" + "=".repeat(70));
            System.out.println("=== Dataset 2 ===");
            DataSet trainingData2 = DataSet.getFixedDataSet2();
            trainAndTest(trainingData2, 2, 7); 
            
            
            Thread.sleep(2000);
            
            
            System.out.println("\n\n" + "=".repeat(70));
            System.out.println("=== Dataset 3 ===");
            DataSet trainingData3 = DataSet.getFixedDataSet3();
            trainAndTest(trainingData3, 3, 9); 
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("Program completed successfully.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void trainAndTest(DataSet trainingData, int datasetNumber, int kValue) {
        try {
            
            List<String> uniqueLabels = trainingData.getUniqueLabels();
            System.out.println("\nDataset " + datasetNumber + " Info:");
            System.out.println("  Number of training points: " + trainingData.size());
            System.out.println("  Classes: " + String.join(", ", uniqueLabels));
            
           
            KNNClassifier classifier = new KNNClassifier(kValue);
            classifier.train(trainingData);
            
           
            int testPointCount = 7;
            List<Point> testPoints = DataSet.generateRandomTestPoints(testPointCount);
            
            System.out.println("\n" + "-".repeat(60));
            System.out.println("CLASSIFICATION RESULTS (Dataset " + datasetNumber + "):");
            System.out.println("-".repeat(60));
            
            List<Point> classifiedPoints = new ArrayList<>();
            
            for (int i = 0; i < testPoints.size(); i++) {
                Point testPoint = testPoints.get(i);
                String predictedClass = classifier.predict(testPoint);
                testPoint.setLabel("Test " + (i+1) + ": " + predictedClass);
                classifiedPoints.add(testPoint);
                List<Point> nearestNeighbors = classifier.getNearestNeighbors(testPoint);
                
                System.out.println("\nTest Point " + (i+1) + ":");
                System.out.printf("  Coordinates: (%.4f, %.4f)%n", 
                               testPoint.getX(), testPoint.getY());
                System.out.println("  Predicted class: " + predictedClass);
                System.out.println("  " + kValue + " nearest neighbors:");
                
                for (int j = 0; j < nearestNeighbors.size(); j++) {
                    Point neighbor = nearestNeighbors.get(j);
                    System.out.printf("    %d. (%.4f, %.4f) [%s] - Distance: %.4f%n",
                                   j + 1, neighbor.getX(), neighbor.getY(), 
                                   neighbor.getLabel(), neighbor.getDistanceToTarget());
                }
            }
            
            Plotter.displayClassificationResults(trainingData, classifiedPoints);
            
        } catch (Exception e) {
            System.err.println("Error in dataset " + datasetNumber + ": " + e.getMessage());
        }
    }
}