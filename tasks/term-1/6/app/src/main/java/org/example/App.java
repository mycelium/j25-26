package org.example;

public class App {

    public static void main(String[] args) throws Exception {
        runScenario0();
        runScenario1();
        runScenario2();
    }

    private static void runScenario0() throws Exception {
        PointClassifier classifier = new PointClassifier();

        classifier.addPoint(1.0, 1.0, 1);
        classifier.addPoint(1.5, 2.0, 1);
        classifier.addPoint(2.0, 1.3, 1);
        classifier.addPoint(2.2, 2.1, 1);

        classifier.addPoint(7.0, 7.0, 2);
        classifier.addPoint(7.5, 8.0, 2);
        classifier.addPoint(8.2, 7.4, 2);
        classifier.addPoint(8.5, 8.1, 2);

        classifier.addPoint(3.0, 8.0, 3);
        classifier.addPoint(3.4, 8.8, 3);
        classifier.addPoint(4.0, 7.5, 3);
        classifier.addPoint(4.2, 8.6, 3);

        double newX = 3.2;
        double newY = 8.1;

        int predictedClass = classifier.classify(newX, newY);

        System.out.println("Scenario 0");
        System.out.println("New point: (" + newX + ", " + newY + ")");
        System.out.println("Predicted class: " + predictedClass);

        classifier.drawPoints(newX, newY, predictedClass, "knn_plot_0.png");
    }

    private static void runScenario1() throws Exception {
        PointClassifier classifier = new PointClassifier();

        classifier.addPoint(1.4, 7.2, 1);
        classifier.addPoint(8.1, 2.5, 1);
        classifier.addPoint(4.7, 6.8, 1);
        classifier.addPoint(2.9, 3.4, 1);
        classifier.addPoint(7.3, 5.1, 1);

        classifier.addPoint(3.6, 8.7, 2);
        classifier.addPoint(6.4, 1.9, 2);
        classifier.addPoint(2.2, 5.6, 2);
        classifier.addPoint(9.0, 4.3, 2);
        classifier.addPoint(5.8, 7.1, 2);

        classifier.addPoint(4.1, 2.6, 3);
        classifier.addPoint(7.6, 8.4, 3);
        classifier.addPoint(1.9, 4.8, 3);
        classifier.addPoint(6.2, 6.5, 3);
        classifier.addPoint(3.3, 1.7, 3);

        double newX = 5.0;
        double newY = 5.0;

        int predictedClass = classifier.classify(newX, newY);

        System.out.println("Scenario 1");
        System.out.println("New point: (" + newX + ", " + newY + ")");
        System.out.println("Predicted class: " + predictedClass);

        classifier.drawPoints(newX, newY, predictedClass, "knn_plot_1.png");
    }

    private static void runScenario2() throws Exception {
        PointClassifier classifier = new PointClassifier();

        classifier.addPoint(1.0, 4.2, 1);
        classifier.addPoint(1.5, 5.8, 1);
        classifier.addPoint(2.0, 3.6, 1);
        classifier.addPoint(2.4, 6.1, 1);
        classifier.addPoint(1.8, 4.9, 1);

        classifier.addPoint(8.1, 4.4, 2);
        classifier.addPoint(8.7, 5.7, 2);
        classifier.addPoint(7.6, 3.8, 2);
        classifier.addPoint(9.0, 4.9, 2);
        classifier.addPoint(8.4, 6.2, 2);

        classifier.addPoint(4.0, 8.2, 3);
        classifier.addPoint(5.2, 9.0, 3);
        classifier.addPoint(6.0, 8.4, 3);
        classifier.addPoint(4.8, 9.4, 3);
        classifier.addPoint(5.5, 7.8, 3);

        double newX = 5.2;
        double newY = 5.0;

        int predictedClass = classifier.classify(newX, newY);

        System.out.println("Scenario 2");
        System.out.println("New point: (" + newX + ", " + newY + ")");
        System.out.println("Predicted class: " + predictedClass);

        classifier.drawPoints(newX, newY, predictedClass, "knn_plot_2.png");
    }
}