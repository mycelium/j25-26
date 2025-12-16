package ru.knn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        int k = 3;
        KNNClassifier classifier = new KNNClassifier(k);

        List<Point> trainingData = generateData(3, 7, 5.,5., true);

        classifier.addTrainingPoints(trainingData);

        // Тестируем классификацию нескольких точек
        List<Point> testData = generateData(10, 1, 4.5, 4.5, false);

        System.out.println("=== Классификация точек методом KNN ===");
        System.out.println("k = " + k);
        System.out.println("\nОбучающие данные:");
        for (Point p : trainingData) {
            System.out.println(p);
        }

        System.out.println("\nРезультаты классификации:");
        for (Point testPoint : testData) {
            String predictedClass = classifier.classify(testPoint);
            System.out.printf("Точка (%.1f, %.1f) → класс %s%n",
                    testPoint.x(), testPoint.y(), predictedClass);


            String filename = String.format(predictedClass + "knn_plot_%.1f_%.1f.png",
                    testPoint.x(), testPoint.y());
            PointPlotter.plotPoints(trainingData, testPoint, filename);
        }

        PointPlotter.plotPoints(trainingData, null, "knn_all_points.png");

        System.out.println("\nГрафики сохранены в файлы .png");
    }


    public static List<Point> generateData(int numOfClasses, int pointsPerClass, double maxX, double maxY, Boolean training) {
        if (training) numOfClasses = Math.min(numOfClasses, 5);
        maxX = maxX > 0 ? maxX : 1;
        maxY = maxY > 0 ? maxY : 1;
        String[] lables = {"A", "B", "C", "D", "E"};

        List<Point> trainingData = new ArrayList<>();
        Random random = new Random();
        for (int classId = 0; classId < numOfClasses; classId++) {
            double centerX = random.nextDouble() * maxX;
            double centerY = random.nextDouble() * maxY;

            for (int i = 0; i < pointsPerClass; i++) {
                double deviationX = (random.nextDouble() - 0.5) * maxX * 0.2;
                double deviationY = (random.nextDouble() - 0.5) * maxY * 0.2;

                double x = centerX + deviationX;
                double y = centerY + deviationY;

                x = Math.max(0, Math.min(maxX, x));
                y = Math.max(0, Math.min(maxY, y));

                trainingData.add(
                    new Point(x, y, training ? lables[classId] : null)
                );

            }
        }

        return trainingData;
    }
}