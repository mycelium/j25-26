package org.example;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Point> train = new ArrayList<>();

        // класс 0
        train.add(new Point(1.0, 1.0, 0));
        train.add(new Point(1.5, 1.2, 0));
        train.add(new Point(0.8, 0.9, 0));
        train.add(new Point(1.2, 0.7, 0));

        // класс 1
        train.add(new Point(4.0, 4.0, 1));
        train.add(new Point(4.2, 3.8, 1));
        train.add(new Point(3.8, 4.1, 1));
        train.add(new Point(4.3, 4.2, 1));

        // класс 2
        train.add(new Point(7.0, 1.0, 2));
        train.add(new Point(7.5, 1.3, 2));
        train.add(new Point(6.8, 0.9, 2));
        train.add(new Point(7.1, 0.7, 2));

        int k = 3;
        KnnClassifier classifier = new KnnClassifier(train, k);

        double newX = 3.0;
        double newY = 3.0;
        int predictedClass = classifier.classify(newX, newY);

        System.out.println("New point: (" + newX + ", " + newY + ")");
        System.out.println("Predicted class: " + predictedClass);

        train.add(new Point(newX, newY, predictedClass));

        double minX = train.get(0).x;
        double maxX = train.get(0).x;
        double minY = train.get(0).y;
        double maxY = train.get(0).y;

        for (Point p : train) {
            if (p.x < minX) minX = p.x;
            if (p.x > maxX) maxX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.y > maxY) maxY = p.y;
        }

        double paddingX = 0.5;
        double paddingY = 0.5;
        minX -= paddingX;
        maxX += paddingX;
        minY -= paddingY;
        maxY += paddingY;

        SimplePlotter.drawPoints(
                train,
                "knn_plot.png",
                600,
                400,
                minX,
                maxX,
                minY,
                maxY
        );

        System.out.println("Image saved to knn_plot.png");
    }
}
