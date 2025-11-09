package org.example;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

public class AppTest {
    @Test
    public void testPointDistance() {
        Point p1 = new Point(0, 0);
        Point p2 = new Point(3, 4);
        assertEquals(5.0, p1.distanceTo(p2), 0.001);
    }

    @Test
    public void testKNNClassification() {
        KNNClassifier classifier = new KNNClassifier(3);

        List<Point> trainingData = Arrays.asList(
                new Point(1, 1, "A"),
                new Point(1, 2, "A"),
                new Point(2, 1, "A"),
                new Point(5, 5, "B"),
                new Point(5, 6, "B"),
                new Point(6, 5, "B")
        );

        classifier.addTrainingPoints(trainingData);

        Point testPoint = new Point(1.5, 1.5); //дб А
        String result = classifier.classify(testPoint);
        assertEquals("A", result);
    }

    @Test
    public void testAppInitialization() {
        App app = new App(3);
        assertNotNull(app);
    }

    @Test
    public void testMultipleClasses() {
        KNNClassifier classifier = new KNNClassifier(3);

        List<Point> trainingData = Arrays.asList(
                new Point(1, 1, "A"),
                new Point(2, 2, "B"),
                new Point(3, 3, "C"),
                new Point(1, 2, "A"),
                new Point(2, 3, "B"),
                new Point(3, 4, "C")
        );

        classifier.addTrainingPoints(trainingData);

        Point testPoint = new Point(2.1, 2.1); //дб В
        String result = classifier.classify(testPoint);
        assertEquals("B", result);
    }
}