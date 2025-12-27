package org.example;

import java.util.*;

public class App {
    public static void main(String[] args) {
        NeighborClassifier classifier = new NeighborClassifier(5);
        Random generator = new Random(42);
        List<Point> dataset = new ArrayList<>();
        createReferenceSamples(classifier, generator, dataset);
        evaluateNewSamples(classifier, generator, dataset);
        Visualizer visualizer = new Visualizer(dataset);
        visualizer.showWindow();
    }

    private static void createReferenceSamples(NeighborClassifier classifier, Random gen, List<Point> dataset) {
        for (int i = 0; i < 12; i++) {
            Point p = new Point(5 + gen.nextGaussian() * 6, 10 + gen.nextGaussian() * 6, "A");
            classifier.addReference(p);
            dataset.add(p);
        }

        for (int i = 0; i < 12; i++) {
            Point p = new Point(90 + gen.nextGaussian() * 7, 90 + gen.nextGaussian() * 7, "C");
            classifier.addReference(p);
            dataset.add(p);
        }

        for (int i = 0; i < 12; i++) {
            Point p = new Point(40 + gen.nextGaussian() * 8, 40 + gen.nextGaussian() * 8, "B");
            classifier.addReference(p);
            dataset.add(p);
        }
    }

    private static void evaluateNewSamples(NeighborClassifier classifier, Random gen, List<Point> dataset) {
        processSample(classifier, 20, 15, dataset);
        processSample(classifier, 85, 70, dataset);
        processSample(classifier, 48, 52, dataset);

        for (int i = 0; i < 5; i++) {
            processSample(classifier, gen.nextDouble() * 100, gen.nextDouble() * 100, dataset);
        }
    }

    private static void processSample(NeighborClassifier classifier, double x, double y, List<Point> dataset) {
        Point unknown = new Point(x, y);
        String prediction = classifier.predict(unknown);
        Point resultPoint = new Point(x, y, prediction);
        dataset.add(resultPoint);
        System.out.printf("точка (%.1f, %.1f) — класс: %s\n", x, y, prediction);
    }
}