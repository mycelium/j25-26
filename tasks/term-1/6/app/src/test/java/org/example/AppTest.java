package org.example;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class AppTest {

    private List<Point> createTrainPoints() {
        List<Point> train = new ArrayList<>();

        // class 0
        train.add(new Point(1.0, 1.0, 0));
        train.add(new Point(1.5, 1.2, 0));
        train.add(new Point(0.8, 0.9, 0));

        // class 1
        train.add(new Point(4.0, 4.0, 1));
        train.add(new Point(4.2, 3.8, 1));
        train.add(new Point(3.8, 4.1, 1));

        // class 2
        train.add(new Point(7.0, 1.0, 2));
        train.add(new Point(7.5, 1.3, 2));
        train.add(new Point(6.8, 0.9, 2));

        return train;
    }

    @Test
    public void testClassifyNearClass0() {
        List<Point> train = createTrainPoints();
        KnnClassifier classifier = new KnnClassifier(train, 3);

        // точка ближе всего к классу 0
        int result = classifier.classify(1.1, 1.0);

        assertEquals(0, result);
    }

    @Test
    public void testClassifyNearClass1() {
        List<Point> train = createTrainPoints();
        KnnClassifier classifier = new KnnClassifier(train, 3);

        // точка ближе всего к классу 1
        int result = classifier.classify(4.0, 3.9);

        assertEquals(1, result);
    }

    @Test
    public void testClassifyNearClass2() {
        List<Point> train = createTrainPoints();
        KnnClassifier classifier = new KnnClassifier(train, 3);

        // точка ближе всего к классу 2
        int result = classifier.classify(7.1, 1.1);

        assertEquals(2, result);
    }

    @Test
    public void testDifferentK() {
        List<Point> train = createTrainPoints();

        KnnClassifier classifierK1 = new KnnClassifier(train, 1);
        KnnClassifier classifierK3 = new KnnClassifier(train, 3);

        int r1 = classifierK1.classify(4.0, 4.0);
        int r3 = classifierK3.classify(4.0, 4.0);

        // оба должны давать класс 1, так как точка очень близко к кластеру 1
        assertEquals(1, r1);
        assertEquals(1, r3);
    }

    @Test
    public void testPointFields() {
        Point p = new Point(2.5, 3.5, 1);

        assertEquals(2.5, p.x, 0.0001);
        assertEquals(3.5, p.y, 0.0001);
        assertEquals(1, p.clazz);
    }
}
