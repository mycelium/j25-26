package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test void testMNISTClassifierCreation() {
        MNISTClassifier classifier = new MNISTClassifier();
        assertNotNull(classifier, "MNISTClassifier should be created successfully");
    }
}