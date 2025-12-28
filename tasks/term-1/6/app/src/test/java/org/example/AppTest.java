// src/test/java/org/example/AppTest.java
package org.example;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class AppTest {

    @Test
    public void appHasAGreeting() {
        App app = new App();
        assertNotNull("app should have a greeting", app.getGreeting());
    }

    @Test
    public void knnShouldClassifyThreeClasses() {
        List<App.Point> data = Arrays.asList(
                new App.Point(1, 1, "X"),
                new App.Point(1.5, 1.5, "X"),
                new App.Point(8, 8, "Y"),
                new App.Point(8.5, 8.5, "Y"),
                new App.Point(4, 5, "Z"),
                new App.Point(4.5, 5.5, "Z"));

        App.KNNClassifier knn = new App.KNNClassifier(data, 3);
        String result = knn.classify(new App.Point(4.2, 5.2, null));
        assertEquals("Z", result);
    }
}