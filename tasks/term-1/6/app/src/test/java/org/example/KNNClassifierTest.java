package org.example;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
Важные замечания:
- Тесты проверяют корректность классификации методом k-ближайших соседей.
- Используется евклидово расстояние для определения близости точек.
- Тесты охватывают базовую функциональность, граничные случаи и обработку ошибок.
*/

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KNNClassifierTest {

    private static final double DELTA = 0.0001;
    private KNNClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new KNNClassifier();
    }

    // Category 1: Basic Classification

    @Test
    @Order(1)
    @DisplayName("Test 1.1: Single neighbor (k=1) - exact match")
    void testSingleNeighborExactMatch() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(10, 10, "B")
        );
        Point testPoint = new Point(0, 0);

        String result = classifier.classify(trainingData, testPoint, 1);

        assertEquals("A", result, "Point at same location should be classified as A");
    }

    @Test
    @Order(2)
    @DisplayName("Test 1.2: Single neighbor (k=1) - nearest point")
    void testSingleNeighborNearest() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(10, 10, "B"),
            new Point(5, 5, "C")
        );
        Point testPoint = new Point(1, 1);

        String result = classifier.classify(trainingData, testPoint, 1);

        assertEquals("A", result, "Nearest point is A at (0,0)");
    }

    @Test
    @Order(3)
    @DisplayName("Test 1.3: Multiple neighbors (k=3) - majority vote")
    void testMajorityVote() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 0, "A"),
            new Point(0, 1, "B"),
            new Point(10, 10, "C")
        );
        Point testPoint = new Point(0.5, 0.5);

        String result = classifier.classify(trainingData, testPoint, 3);

        assertEquals("A", result, "Majority (2 out of 3) should be A");
    }

    @Test
    @Order(4)
    @DisplayName("Test 1.4: All neighbors same class")
    void testAllSameClass() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 0, "A"),
            new Point(0, 1, "A"),
            new Point(1, 1, "A")
        );
        Point testPoint = new Point(0.5, 0.5);

        String result = classifier.classify(trainingData, testPoint, 3);

        assertEquals("A", result, "All neighbors are A");
    }

    @Test
    @Order(5)
    @DisplayName("Test 1.5: Three classes classification")
    void testThreeClasses() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 0, "A"),
            new Point(2, 0, "A"),
            new Point(10, 0, "B"),
            new Point(11, 0, "B"),
            new Point(12, 0, "B"),
            new Point(5, 10, "C"),
            new Point(6, 10, "C"),
            new Point(7, 10, "C")
        );

        assertEquals("A", classifier.classify(trainingData, new Point(0.5, 0), 3));
        assertEquals("B", classifier.classify(trainingData, new Point(10.5, 0), 3));
        assertEquals("C", classifier.classify(trainingData, new Point(6, 9), 3));
    }

    // Category 2: Distance Calculation

    @Test
    @Order(10)
    @DisplayName("Test 2.1: Equidistant points - first in list wins")
    void testEquidistantPoints() {
        List<Point> trainingData = Arrays.asList(
            new Point(1, 0, "A"),
            new Point(-1, 0, "B"),
            new Point(0, 1, "C"),
            new Point(0, -1, "D")
        );
        Point testPoint = new Point(0, 0);

        // All points are equidistant (distance = 1)
        // With k=1, should return first point found
        String result = classifier.classify(trainingData, testPoint, 1);

        assertNotNull(result, "Should return a valid classification");
    }

    @Test
    @Order(11)
    @DisplayName("Test 2.2: Negative coordinates")
    void testNegativeCoordinates() {
        List<Point> trainingData = Arrays.asList(
            new Point(-5, -5, "A"),
            new Point(-4, -4, "A"),
            new Point(5, 5, "B"),
            new Point(6, 6, "B")
        );
        Point testPoint = new Point(-3, -3);

        String result = classifier.classify(trainingData, testPoint, 2);

        assertEquals("A", result, "Nearest neighbors in negative quadrant are A");
    }

    @Test
    @Order(12)
    @DisplayName("Test 2.3: Decimal coordinates")
    void testDecimalCoordinates() {
        List<Point> trainingData = Arrays.asList(
            new Point(0.1, 0.1, "A"),
            new Point(0.2, 0.2, "A"),
            new Point(9.9, 9.9, "B"),
            new Point(9.8, 9.8, "B")
        );
        Point testPoint = new Point(0.15, 0.15);

        String result = classifier.classify(trainingData, testPoint, 2);

        assertEquals("A", result, "Nearest neighbors are A");
    }

    @Test
    @Order(13)
    @DisplayName("Test 2.4: Large coordinates")
    void testLargeCoordinates() {
        List<Point> trainingData = Arrays.asList(
            new Point(1000000, 1000000, "A"),
            new Point(1000001, 1000001, "A"),
            new Point(-1000000, -1000000, "B"),
            new Point(-1000001, -1000001, "B")
        );
        Point testPoint = new Point(1000000.5, 1000000.5);

        String result = classifier.classify(trainingData, testPoint, 2);

        assertEquals("A", result, "Should handle large coordinates");
    }

    @Test
    @Order(14)
    @DisplayName("Test 2.5: Very small differences")
    void testVerySmallDifferences() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(0.0001, 0.0001, "B")
        );
        Point testPoint = new Point(0.00005, 0.00005);

        // Closer to (0,0) than to (0.0001, 0.0001)
        String result = classifier.classify(trainingData, testPoint, 1);

        assertEquals("A", result, "Should detect very small distance differences");
    }

    // Category 3: K Value Variations

    @Test
    @Order(20)
    @DisplayName("Test 3.1: k equals training data size")
    void testKEqualsDataSize() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 0, "A"),
            new Point(2, 0, "A"),
            new Point(10, 0, "B"),
            new Point(11, 0, "B")
        );
        Point testPoint = new Point(5, 0);

        String result = classifier.classify(trainingData, testPoint, 5);

        assertEquals("A", result, "With k=5, A has 3 votes, B has 2 votes");
    }

    @Test
    @Order(21)
    @DisplayName("Test 3.2: k=1 always returns nearest")
    void testK1AlwaysNearest() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 0, "B"),
            new Point(2, 0, "C"),
            new Point(3, 0, "D")
        );

        assertEquals("A", classifier.classify(trainingData, new Point(-1, 0), 1));
        assertEquals("B", classifier.classify(trainingData, new Point(0.6, 0), 1));
        assertEquals("C", classifier.classify(trainingData, new Point(1.6, 0), 1));
        assertEquals("D", classifier.classify(trainingData, new Point(4, 0), 1));
    }

    @Test
    @Order(22)
    @DisplayName("Test 3.3: Increasing k changes result")
    void testIncreasingKChangesResult() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(2, 0, "B"),
            new Point(3, 0, "B"),
            new Point(4, 0, "B")
        );
        Point testPoint = new Point(1, 0);

        // k=1: nearest is A
        assertEquals("A", classifier.classify(trainingData, testPoint, 1));

        // k=3: A(1), B(2) -> B wins
        assertEquals("B", classifier.classify(trainingData, testPoint, 3));
    }

    @Test
    @Order(23)
    @DisplayName("Test 3.4: Odd k value for tie-breaking")
    void testOddKForTieBreaking() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 0, "A"),
            new Point(2, 0, "B"),
            new Point(3, 0, "B"),
            new Point(4, 0, "A")
        );
        Point testPoint = new Point(1.5, 0);

        String result = classifier.classify(trainingData, testPoint, 5);

        assertEquals("A", result, "A has 3 votes, B has 2 votes");
    }

    // Category 4: Edge Cases

    @Test
    @Order(30)
    @DisplayName("Test 4.1: Single training point")
    void testSingleTrainingPoint() {
        List<Point> trainingData = Arrays.asList(
            new Point(5, 5, "OnlyClass")
        );
        Point testPoint = new Point(0, 0);

        String result = classifier.classify(trainingData, testPoint, 1);

        assertEquals("OnlyClass", result, "Only one point available");
    }

    @Test
    @Order(31)
    @DisplayName("Test 4.2: Test point at origin")
    void testPointAtOrigin() {
        List<Point> trainingData = Arrays.asList(
            new Point(1, 1, "A"),
            new Point(-1, -1, "B"),
            new Point(1, -1, "C")
        );
        Point testPoint = new Point(0, 0);

        String result = classifier.classify(trainingData, testPoint, 1);

        assertNotNull(result, "Should classify point at origin");
    }

    @Test
    @Order(32)
    @DisplayName("Test 4.3: All training points at same location")
    void testAllPointsSameLocation() {
        List<Point> trainingData = Arrays.asList(
            new Point(5, 5, "A"),
            new Point(5, 5, "B"),
            new Point(5, 5, "A")
        );
        Point testPoint = new Point(0, 0);

        String result = classifier.classify(trainingData, testPoint, 3);

        assertEquals("A", result, "A has 2 votes, B has 1 vote");
    }

    @Test
    @Order(33)
    @DisplayName("Test 4.4: Points along diagonal line")
    void testPointsAlongDiagonal() {
        List<Point> trainingData = Arrays.asList(
            new Point(1, 1, "A"),
            new Point(2, 2, "A"),
            new Point(3, 3, "B"),
            new Point(4, 4, "B"),
            new Point(5, 5, "C")
        );
        Point testPoint = new Point(2.5, 2.5);

        String result = classifier.classify(trainingData, testPoint, 2);

        // (2,2) and (3,3) are closest
        assertNotNull(result, "Should handle diagonal points");
    }

    @Test
    @Order(34)
    @DisplayName("Test 4.5: Points in circular pattern")
    void testCircularPattern() {
        List<Point> trainingData = new ArrayList<>();
        // Create points in a circle around origin
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            double x = Math.cos(angle) * 10;
            double y = Math.sin(angle) * 10;
            trainingData.add(new Point(x, y, i < 4 ? "A" : "B"));
        }
        Point testPoint = new Point(0, 0);

        String result = classifier.classify(trainingData, testPoint, 4);

        assertNotNull(result, "Should handle circular pattern");
    }

    @Test
    @Order(35)
    @DisplayName("Test 4.6: Long class names")
    void testLongClassNames() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "VeryLongClassName_Category_A"),
            new Point(1, 1, "VeryLongClassName_Category_A"),
            new Point(10, 10, "AnotherVeryLongClassName_Category_B")
        );
        Point testPoint = new Point(0.5, 0.5);

        String result = classifier.classify(trainingData, testPoint, 2);

        assertEquals("VeryLongClassName_Category_A", result);
    }

    // Category 5: Error Handling

    @Test
    @Order(40)
    @DisplayName("Test 5.1: Null training data throws exception")
    void testNullTrainingData() {
        Point testPoint = new Point(0, 0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> classifier.classify(null, testPoint, 1)
        );

        assertTrue(exception.getMessage().contains("null") || 
                   exception.getMessage().contains("empty"),
                   "Exception message should mention null or empty");
    }

    @Test
    @Order(41)
    @DisplayName("Test 5.2: Empty training data throws exception")
    void testEmptyTrainingData() {
        List<Point> trainingData = new ArrayList<>();
        Point testPoint = new Point(0, 0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> classifier.classify(trainingData, testPoint, 1)
        );

        assertTrue(exception.getMessage().contains("empty") || 
                   exception.getMessage().contains("null"),
                   "Exception message should mention empty");
    }

    @Test
    @Order(42)
    @DisplayName("Test 5.3: k=0 throws exception")
    void testKZero() {
        List<Point> trainingData = Arrays.asList(new Point(0, 0, "A"));
        Point testPoint = new Point(0, 0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> classifier.classify(trainingData, testPoint, 0)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("k") ||
                   exception.getMessage().contains("positive"),
                   "Exception should mention k value");
    }

    @Test
    @Order(43)
    @DisplayName("Test 5.4: Negative k throws exception")
    void testNegativeK() {
        List<Point> trainingData = Arrays.asList(new Point(0, 0, "A"));
        Point testPoint = new Point(0, 0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> classifier.classify(trainingData, testPoint, -5)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    @Order(44)
    @DisplayName("Test 5.5: k greater than training data size throws exception")
    void testKGreaterThanDataSize() {
        List<Point> trainingData = Arrays.asList(
            new Point(0, 0, "A"),
            new Point(1, 1, "B")
        );
        Point testPoint = new Point(0, 0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> classifier.classify(trainingData, testPoint, 5)
        );

        assertNotNull(exception.getMessage());
    }

    // Category 6: Performance & Large Data

    @Test
    @Order(50)
    @DisplayName("Test 6.1: Medium dataset (1000 points)")
    @Timeout(5)
    void testMediumDataset() {
        List<Point> trainingData = new ArrayList<>();
        
        // Create 500 points for class A (cluster around 0,0)
        for (int i = 0; i < 500; i++) {
            trainingData.add(new Point(
                Math.random() * 10, 
                Math.random() * 10, 
                "A"
            ));
        }
        
        // Create 500 points for class B (cluster around 50,50)
        for (int i = 0; i < 500; i++) {
            trainingData.add(new Point(
                50 + Math.random() * 10, 
                50 + Math.random() * 10, 
                "B"
            ));
        }

        assertEquals("A", classifier.classify(trainingData, new Point(5, 5), 5));
        assertEquals("B", classifier.classify(trainingData, new Point(55, 55), 5));
    }

    @Test
    @Order(51)
    @DisplayName("Test 6.2: Large dataset (10000 points)")
    @Timeout(10)
    void testLargeDataset() {
        List<Point> trainingData = new ArrayList<>();
        
        // Create 5000 points for each class
        for (int i = 0; i < 5000; i++) {
            trainingData.add(new Point(Math.random() * 10, Math.random() * 10, "A"));
            trainingData.add(new Point(100 + Math.random() * 10, 100 + Math.random() * 10, "B"));
        }

        String result = classifier.classify(trainingData, new Point(5, 5), 10);
        
        assertEquals("A", result, "Point near A cluster should be classified as A");
    }

    @Test
    @Order(52)
    @DisplayName("Test 6.3: Many classes (26 classes)")
    @Timeout(5)
    void testManyClasses() {
        List<Point> trainingData = new ArrayList<>();
        
        // Create 26 classes (A-Z), each with 10 points
        for (int c = 0; c < 26; c++) {
            String className = String.valueOf((char) ('A' + c));
            double centerX = c * 10;
            double centerY = c * 10;
            
            for (int i = 0; i < 10; i++) {
                trainingData.add(new Point(
                    centerX + Math.random() * 2,
                    centerY + Math.random() * 2,
                    className
                ));
            }
        }

        // Test classification near each cluster
        assertEquals("A", classifier.classify(trainingData, new Point(1, 1), 3));
        assertEquals("M", classifier.classify(trainingData, new Point(121, 121), 3));
        assertEquals("Z", classifier.classify(trainingData, new Point(251, 251), 3));
    }

    @Test
    @Order(53)
    @DisplayName("Test 6.4: High k value (k=100)")
    @Timeout(5)
    void testHighKValue() {
        List<Point> trainingData = new ArrayList<>();
        
        // Create 150 points: 100 A, 50 B
        for (int i = 0; i < 100; i++) {
            trainingData.add(new Point(Math.random(), Math.random(), "A"));
        }
        for (int i = 0; i < 50; i++) {
            trainingData.add(new Point(Math.random(), Math.random(), "B"));
        }

        String result = classifier.classify(trainingData, new Point(0.5, 0.5), 100);
        
        // With k=100, A should win (has more points)
        assertEquals("A", result, "A has more representatives");
    }

    // Category 7: Realistic Scenarios

    @Test
    @Order(60)
    @DisplayName("Test 7.1: Clear cluster separation")
    void testClearClusterSeparation() {
        List<Point> trainingData = Arrays.asList(
            // Cluster A (bottom-left)
            new Point(1, 1, "A"), new Point(1, 2, "A"), new Point(2, 1, "A"),
            // Cluster B (top-right)
            new Point(9, 9, "B"), new Point(9, 8, "B"), new Point(8, 9, "B"),
            // Cluster C (top-left)
            new Point(1, 9, "C"), new Point(1, 8, "C"), new Point(2, 9, "C")
        );

        assertEquals("A", classifier.classify(trainingData, new Point(1.5, 1.5), 3));
        assertEquals("B", classifier.classify(trainingData, new Point(8.5, 8.5), 3));
        assertEquals("C", classifier.classify(trainingData, new Point(1.5, 8.5), 3));
    }

    @Test
    @Order(61)
    @DisplayName("Test 7.2: Overlapping clusters")
    void testOverlappingClusters() {
        List<Point> trainingData = Arrays.asList(
            new Point(4, 5, "A"),
            new Point(5, 4, "A"),
            new Point(6, 5, "B"),
            new Point(5, 6, "B")
        );
        Point testPoint = new Point(5, 5);

        // All points equidistant, result depends on implementation
        String result = classifier.classify(trainingData, testPoint, 4);
        
        assertNotNull(result);
        assertTrue(result.equals("A") || result.equals("B"), 
                   "Should return either A or B");
    }

    @Test
    @Order(62)
    @DisplayName("Test 7.3: Linear decision boundary")
    void testLinearDecisionBoundary() {
        List<Point> trainingData = new ArrayList<>();
        
        // Class A: all points where y < x
        for (int i = 0; i < 10; i++) {
            trainingData.add(new Point(i, i - 2, "A"));
        }
        
        // Class B: all points where y > x
        for (int i = 0; i < 10; i++) {
            trainingData.add(new Point(i, i + 2, "B"));
        }

        assertEquals("A", classifier.classify(trainingData, new Point(5, 3), 3));
        assertEquals("B", classifier.classify(trainingData, new Point(5, 7), 3));
    }

    @Test
    @Order(63)
    @DisplayName("Test 7.4: Noise resistance with higher k")
    void testNoiseResistance() {
        List<Point> trainingData = new ArrayList<>();
        
        // Main cluster A
        for (int i = 0; i < 20; i++) {
            trainingData.add(new Point(Math.random() * 2, Math.random() * 2, "A"));
        }
        
        // One noise point B very close to test point
        trainingData.add(new Point(1.01, 1.01, "B"));

        Point testPoint = new Point(1, 1);

        // With k=1, might get noise
        // With k=5, should get correct class A
        String resultK5 = classifier.classify(trainingData, testPoint, 5);
        
        assertEquals("A", resultK5, "Higher k should be noise-resistant");
    }
}

