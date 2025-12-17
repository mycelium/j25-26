package org.example;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MNISTClassifierTest {

    @Test
    void testModelCreation() {
        try {
            MultiLayerNetwork model = MNISTClassifier.createModel();
            assertNotNull(model, "Model should be created successfully");
            assertTrue(model.getLayers().length > 0, "Model should have layers");
        } catch (Exception e) {
            fail("Model creation should not throw exception: " + e.getMessage());
        }
    }

    @Test
    void appHasAGreeting() {
        App classUnderTest = new App();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
        assertEquals("MNIST Image Classification Project", classUnderTest.getGreeting());
    }
}