/*
 * Тесты классификатора KNN
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

        // Проверка евклидового расстояния: sqrt((3-0)^2 + (4-0)^2) = 5
        assertEquals(5.0, p1.distanceTo(p2), 0.001);
    }

    @Test
    public void testKNNClassification() {
        // Создание обучающих данных
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 1, "A"),
            new Point(10, 10, "B"),
            new Point(11, 11, "B")
        );

        KNNClassifier classifier = new KNNClassifier(trainingData, 1);

        // Тестовая точка, близкая к классу A
        Point testPointA = new Point(0.5, 0.5);
        assertEquals("A", classifier.classify(testPointA));

        // Тестовая точка, близкая к классу B
        Point testPointB = new Point(10.5, 10.5);
        assertEquals("B", classifier.classify(testPointB));
    }

    @Test
    public void testKNNWithMajorityVoting() {
        // Создание обучающих данных, где один класс доминирует
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(0.1, 0.1, "A"),
            new Point(0.2, 0.2, "A"),
            new Point(10, 10, "B"),
            new Point(10.1, 10.1, "B")
        );

        KNNClassifier classifier = new KNNClassifier(trainingData, 3);

        // Тестовая точка должна быть классифицирована как A (голосование большинства)
        Point testPoint = new Point(1, 1);
        assertEquals("A", classifier.classify(testPoint));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKNNWithInvalidK() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 1, "A")
        );

        // Должно быть выброшено исключение при k = 0
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

        assertEquals(15, data.size()); // 5 точек на класс * 3 класса

        // Проверка наличия всех классов
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
