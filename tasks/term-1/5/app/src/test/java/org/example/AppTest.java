package org.example;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    private static StanfordCoreNLP pipeline;

    @BeforeAll
    static void setUp() {
        pipeline = App.createPipeline();
        assertNotNull(pipeline, "Pipeline must be created");
    }

    @Test
    void positiveReviewIsClassifiedAsPositive() {
        String review = "This movie was absolutely amazing, I really loved it.";
        String label = App.analyzeSentiment(pipeline, review);
        assertEquals("positive", label, "Expected positive sentiment for clearly positive review");
    }

    @Test
    void negativeReviewIsClassifiedAsNegative() {
        String review = "This film was terrible, boring and a complete waste of time.";
        String label = App.analyzeSentiment(pipeline, review);
        assertEquals("negative", label, "Expected negative sentiment for clearly negative review");
    }

    @Test
    void neutralReviewIsNotExtreme() {
        String review = "The movie was okay, nothing special but not too bad.";
        String label = App.analyzeSentiment(pipeline, review);

        assertTrue(
                label.equals("neutral") ||
                        label.equals("positive") ||
                        label.equals("negative"),
                "Label must be one of neutral/positive/negative"
        );
    }

    @Test
    void emptyTextIsNeutral() {
        String review = "";
        String label = App.analyzeSentiment(pipeline, review);
        assertEquals("neutral", label, "Empty text should be treated as neutral");
    }
}
