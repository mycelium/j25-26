/*
 * KNN Classifier Tests
 */
package org.example;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AppTest {

    @Test
    public void testPointCreationAndDistance() {
        Point p1 = new Point(0, 0, "A");
        Point p2 = new Point(3, 4, "B");

        assertEquals(0.0, p1.getX(), 0.001);
        assertEquals(0.0, p1.getY(), 0.001);
        assertEquals("A", p1.getLabel());

        // Test Euclidean distance: sqrt((3-0)^2 + (4-0)^2) = 5
        assertEquals(5.0, p1.distanceTo(p2), 0.001);
    }

    @Test
    public void testKNNClassification() {
        // Create training data
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 1, "A"),
            new Point(10, 10, "B"),
            new Point(11, 11, "B")
        );

        KNNClassifier classifier = new KNNClassifier(trainingData, 1);

        // Test point close to class A
        Point testPointA = new Point(0.5, 0.5);
        assertEquals("A", classifier.classify(testPointA));

        // Test point close to class B
        Point testPointB = new Point(10.5, 10.5);
        assertEquals("B", classifier.classify(testPointB));
    }

    @Test
    public void testKNNWithMajorityVoting() {
        // Create training data where one class dominates
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(0.1, 0.1, "A"),
            new Point(0.2, 0.2, "A"),
            new Point(10, 10, "B"),
            new Point(10.1, 10.1, "B")
        );

        KNNClassifier classifier = new KNNClassifier(trainingData, 3);

        // Test point should be classified as A (majority vote)
        Point testPoint = new Point(1, 1);
        assertEquals("A", classifier.classify(testPoint));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKNNWithInvalidK() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 1, "A")
        );

        // Should throw exception for k=0
        new KNNClassifier(trainingData, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKNNWithNullTrainingData() {
        new KNNClassifier(null, 1);
    }

    @Test
    public void testDataGenerator() {
        DataGenerator generator = new DataGenerator();
        List<Point> data = generator.generateSampleData(5);

        assertEquals(15, data.size()); // 5 points per class * 3 classes

        // Check that all classes are present
        boolean hasA = false, hasB = false, hasC = false;
        for (Point point : data) {
            String label = point.getLabel();
            if ("A".equals(label)) hasA = true;
            if ("B".equals(label)) hasB = true;
            if ("C".equals(label)) hasC = true;
        }

        assertTrue("Should have class A", hasA);
        assertTrue("Should have class B", hasB);
        assertTrue("Should have class C", hasC);
    }
}
