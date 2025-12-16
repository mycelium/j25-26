package knn;

import java.util.*;

public class App {
    public static void main(String[] args) {
        List<Point> trainingData = generatePoints();

        Point newPoint = generatePoint();

        KNN knn = new KNN(10);
        knn.setDataset(trainingData);

        String predictedClass = knn.predict(newPoint);
        System.out.println("New point: (" +
                String.format("%.2f", newPoint.getX()) + ", " +
                String.format("%.2f", newPoint.getY()) + ")");
        System.out.println("Predicted class: " + predictedClass);

        trainingData.add(new Point(newPoint.getX(), newPoint.getY(), predictedClass));

        Plotter.plot(trainingData);
    }

    private static List<Point> generatePoints() {
        Random rand = new Random();
        List<Point> points = new ArrayList<>();

        int numClasses = 3;
        System.out.println("Number of classes: " + numClasses);

        for (int c = 0; c < numClasses; c++) {
            String label = "Class_" + (char) ('A' + c);

            double centerX = rand.nextDouble() * 20 - 10;
            double centerY = rand.nextDouble() * 20 - 10;

            int pointsPerClass = 30;
            for (int i = 0; i < pointsPerClass; i++) {
                double x = centerX + rand.nextDouble() * 2 - 1;
                double y = centerY + rand.nextDouble() * 2 - 1;
                points.add(new Point(x, y, label));
            }
        }

        return points;
    }

    private static Point generatePoint() {
        Random rand = new Random();
        double x = rand.nextDouble() * 20 - 10;
        double y = rand.nextDouble() * 20 - 10;
        return new Point(x, y, null);
    }
}
