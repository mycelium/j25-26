package org.example;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KNNClassifierTest {

    private List<LabeledPoint> createSimpleDataset() {
        List<LabeledPoint> points = new ArrayList<>();

        // Класс A вокруг (0,0)
        points.add(new LabeledPoint(0, 0, "A"));
        points.add(new LabeledPoint(1, 0, "A"));
        points.add(new LabeledPoint(0, 1, "A"));

        // Класс B вокруг (5,5)
        points.add(new LabeledPoint(5, 5, "B"));
        points.add(new LabeledPoint(6, 5, "B"));
        points.add(new LabeledPoint(5, 6, "B"));

        return points;
    }

    @Test
    void predictReturnsClassAForPointNearClusterA() {
        List<LabeledPoint> data = createSimpleDataset();
        KNNClassifier knn = new KNNClassifier(data, 3);

        String predicted = knn.predict(0.2, 0.1);

        assertEquals("A", predicted);
    }

    @Test
    void predictReturnsClassBForPointNearClusterB() {
        List<LabeledPoint> data = createSimpleDataset();
        KNNClassifier knn = new KNNClassifier(data, 3);

        String predicted = knn.predict(5.2, 5.1);

        assertEquals("B", predicted);
    }

    @Test
    void kIsClampedToDatasetSize() {
        List<LabeledPoint> data = createSimpleDataset();
        // просим k больше, чем точек
        KNNClassifier knn = new KNNClassifier(data, 100);

        String predicted = knn.predict(0.1, 0.0);

        assertEquals("A", predicted);
    }

    @Test
    void throwsOnEmptyTrainingSet() {
        List<LabeledPoint> empty = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,
                () -> new KNNClassifier(empty, 3));
    }

    @Test
    void throwsOnNonPositiveK() {
        List<LabeledPoint> data = createSimpleDataset();
        assertThrows(IllegalArgumentException.class,
                () -> new KNNClassifier(data, 0));
    }
}
