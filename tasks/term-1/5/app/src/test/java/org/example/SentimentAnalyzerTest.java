package org.example;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/*
Важные замечания:
- Тесты проверяют корректность анализа тональности текста с использованием Stanford CoreNLP.
- Инициализация pipeline занимает несколько секунд, поэтому используется @BeforeAll.
- Тесты охватывают позитивные, негативные и нейтральные тексты,
  граничные случаи и обработку ошибок.
*/

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SentimentAnalyzerTest {

    private static SentimentAnalyzer analyzer;

    @BeforeAll
    static void setUp() {
        System.out.println("Initializing Stanford CoreNLP pipeline (this may take a few seconds)...");
        analyzer = new SentimentAnalyzer();
        System.out.println("Pipeline initialized.");
    }

    // Category 1: Positive Sentiment

    @Test
    @Order(1)
    @DisplayName("Test 1.1: Simple positive sentence")
    void testSimplePositive() {
        String result = analyzer.analyze("I love this product!");
        assertEquals("positive", result, "Should detect positive sentiment");
    }

    @Test
    @Order(2)
    @DisplayName("Test 1.2: Very positive sentence")
    void testVeryPositive() {
        String result = analyzer.analyze("This is absolutely amazing and wonderful!");
        assertEquals("positive", result, "Should detect positive sentiment");
    }

    @Test
    @Order(3)
    @DisplayName("Test 1.3: Positive with enthusiasm")
    void testPositiveEnthusiasm() {
        String result = analyzer.analyze("Great job! Excellent work! I am so happy!");
        assertEquals("positive", result, "Should detect positive sentiment");
    }

    @Test
    @Order(4)
    @DisplayName("Test 1.4: Positive review")
    void testPositiveReview() {
        String result = analyzer.analyze("The movie was fantastic and the actors were brilliant.");
        assertEquals("positive", result, "Should detect positive sentiment in review");
    }

    @Test
    @Order(5)
    @DisplayName("Test 1.5: Positive with superlatives")
    void testPositiveSuperlatives() {
        String result = analyzer.analyze("This is the best experience I have ever had.");
        assertEquals("positive", result, "Should detect positive sentiment with superlatives");
    }

    // Category 2: Negative Sentiment

    @Test
    @Order(10)
    @DisplayName("Test 2.1: Simple negative sentence")
    void testSimpleNegative() {
        String result = analyzer.analyze("I hate this product.");
        assertEquals("negative", result, "Should detect negative sentiment");
    }

    @Test
    @Order(11)
    @DisplayName("Test 2.2: Very negative sentence")
    void testVeryNegative() {
        String result = analyzer.analyze("This is terrible and awful!");
        assertEquals("negative", result, "Should detect negative sentiment");
    }

    @Test
    @Order(12)
    @DisplayName("Test 2.3: Negative complaint")
    void testNegativeComplaint() {
        String result = analyzer.analyze("The service was horrible. I am very disappointed.");
        assertEquals("negative", result, "Should detect negative sentiment in complaint");
    }

    @Test
    @Order(13)
    @DisplayName("Test 2.4: Negative review")
    void testNegativeReview() {
        String result = analyzer.analyze("The movie was boring and the plot was confusing.");
        assertEquals("negative", result, "Should detect negative sentiment in review");
    }

    @Test
    @Order(14)
    @DisplayName("Test 2.5: Strong negative words")
    void testStrongNegative() {
        String result = analyzer.analyze("This is the worst thing I have ever seen.");
        assertEquals("negative", result, "Should detect negative sentiment with strong words");
    }

    // Category 3: Neutral Sentiment

    @Test
    @Order(20)
    @DisplayName("Test 3.1: Factual statement")
    void testFactualStatement() {
        String result = analyzer.analyze("The meeting is scheduled for tomorrow.");
        assertEquals("neutral", result, "Factual statement should be neutral");
    }

    @Test
    @Order(21)
    @DisplayName("Test 3.2: Simple description")
    void testSimpleDescription() {
        String result = analyzer.analyze("The book has 300 pages.");
        assertEquals("neutral", result, "Simple description should be neutral");
    }

    @Test
    @Order(22)
    @DisplayName("Test 3.3: Neutral question")
    void testNeutralQuestion() {
        String result = analyzer.analyze("What time does the store open?");
        assertEquals("neutral", result, "Neutral question should be neutral");
    }

    // Category 4: Edge Cases - Empty and Null

    @Test
    @Order(30)
    @DisplayName("Test 4.1: Null input")
    void testNullInput() {
        String result = analyzer.analyze(null);
        assertEquals("neutral", result, "Null input should return neutral");
    }

    @Test
    @Order(31)
    @DisplayName("Test 4.2: Empty string")
    void testEmptyString() {
        String result = analyzer.analyze("");
        assertEquals("neutral", result, "Empty string should return neutral");
    }

    @Test
    @Order(32)
    @DisplayName("Test 4.3: Whitespace only")
    void testWhitespaceOnly() {
        String result = analyzer.analyze("   \t\n   ");
        assertEquals("neutral", result, "Whitespace only should return neutral");
    }

    @Test
    @Order(33)
    @DisplayName("Test 4.4: Single word positive")
    void testSingleWordPositive() {
        String result = analyzer.analyze("Amazing!");
        assertNotNull(result, "Should handle single word");
    }

    @Test
    @Order(34)
    @DisplayName("Test 4.5: Single word negative")
    void testSingleWordNegative() {
        String result = analyzer.analyze("Terrible!");
        assertNotNull(result, "Should handle single word");
    }

    // Category 5: Multiple Sentences

    @Test
    @Order(40)
    @DisplayName("Test 5.1: Multiple positive sentences")
    void testMultiplePositiveSentences() {
        String result = analyzer.analyze("I love this. It is amazing. Best purchase ever.");
        assertEquals("positive", result, "Multiple positive sentences should be positive");
    }

    @Test
    @Order(41)
    @DisplayName("Test 5.2: Multiple negative sentences")
    void testMultipleNegativeSentences() {
        String result = analyzer.analyze("This is awful and terrible. I absolutely hate it.");
        assertEquals("negative", result, "Multiple negative sentences should be negative");
    }

    @Test
    @Order(42)
    @DisplayName("Test 5.3: Mixed sentiment - mostly positive")
    void testMixedMostlyPositive() {
        String result = analyzer.analyze("The food was okay but the service was absolutely fantastic and wonderful.");
        // Longest sentence determines sentiment
        assertNotNull(result, "Should handle mixed sentiment");
    }

    @Test
    @Order(43)
    @DisplayName("Test 5.4: Mixed sentiment - mostly negative")
    void testMixedMostlyNegative() {
        String result = analyzer.analyze("Good start but then everything went terribly wrong and I was very disappointed.");
        assertNotNull(result, "Should handle mixed sentiment");
    }

    // Category 6: Detailed Sentiment Analysis

    @Test
    @Order(50)
    @DisplayName("Test 6.1: Detailed positive analysis")
    void testDetailedPositive() {
        String result = analyzer.analyzeDetailed("I absolutely love this!");
        assertTrue(
            result.equals("Positive") || result.equals("Very positive"),
            "Detailed analysis should return Positive or Very positive"
        );
    }

    @Test
    @Order(51)
    @DisplayName("Test 6.2: Detailed negative analysis")
    void testDetailedNegative() {
        String result = analyzer.analyzeDetailed("I absolutely hate this!");
        assertTrue(
            result.equals("Negative") || result.equals("Very negative"),
            "Detailed analysis should return Negative or Very negative"
        );
    }

    @Test
    @Order(52)
    @DisplayName("Test 6.3: Detailed neutral analysis")
    void testDetailedNeutral() {
        String result = analyzer.analyzeDetailed("The weather is cloudy today.");
        assertEquals("Neutral", result, "Detailed analysis should return Neutral");
    }

    // Category 7: Simplify Sentiment Method

    @Test
    @Order(60)
    @DisplayName("Test 7.1: Simplify 'Very positive' to 'positive'")
    void testSimplifyVeryPositive() {
        String result = analyzer.simplifySentiment("Very positive");
        assertEquals("positive", result);
    }

    @Test
    @Order(61)
    @DisplayName("Test 7.2: Simplify 'Positive' to 'positive'")
    void testSimplifyPositive() {
        String result = analyzer.simplifySentiment("Positive");
        assertEquals("positive", result);
    }

    @Test
    @Order(62)
    @DisplayName("Test 7.3: Simplify 'Very negative' to 'negative'")
    void testSimplifyVeryNegative() {
        String result = analyzer.simplifySentiment("Very negative");
        assertEquals("negative", result);
    }

    @Test
    @Order(63)
    @DisplayName("Test 7.4: Simplify 'Negative' to 'negative'")
    void testSimplifyNegative() {
        String result = analyzer.simplifySentiment("Negative");
        assertEquals("negative", result);
    }

    @Test
    @Order(64)
    @DisplayName("Test 7.5: Simplify 'Neutral' to 'neutral'")
    void testSimplifyNeutral() {
        String result = analyzer.simplifySentiment("Neutral");
        assertEquals("neutral", result);
    }

    @Test
    @Order(65)
    @DisplayName("Test 7.6: Simplify null to 'neutral'")
    void testSimplifyNull() {
        String result = analyzer.simplifySentiment(null);
        assertEquals("neutral", result);
    }

    @Test
    @Order(66)
    @DisplayName("Test 7.7: Simplify unknown to 'neutral'")
    void testSimplifyUnknown() {
        String result = analyzer.simplifySentiment("Unknown");
        assertEquals("neutral", result);
    }

    // Category 8: Batch Analysis

    @Test
    @Order(70)
    @DisplayName("Test 8.1: Batch analysis with multiple texts")
    void testBatchAnalysis() {
        List<String> texts = Arrays.asList(
            "I love this!",
            "I hate this!",
            "The sky is blue."
        );

        Map<String, String> results = analyzer.analyzeBatch(texts);

        assertEquals(3, results.size(), "Should have 3 results");
        assertEquals("positive", results.get("I love this!"));
        assertEquals("negative", results.get("I hate this!"));
        assertEquals("neutral", results.get("The sky is blue."));
    }

    @Test
    @Order(71)
    @DisplayName("Test 8.2: Batch analysis with empty list")
    void testBatchAnalysisEmpty() {
        List<String> texts = new ArrayList<>();

        Map<String, String> results = analyzer.analyzeBatch(texts);

        assertNotNull(results, "Should return non-null map");
        assertTrue(results.isEmpty(), "Should return empty map for empty input");
    }

    @Test
    @Order(72)
    @DisplayName("Test 8.3: Batch analysis with null list")
    void testBatchAnalysisNull() {
        Map<String, String> results = analyzer.analyzeBatch(null);

        assertNotNull(results, "Should return non-null map");
        assertTrue(results.isEmpty(), "Should return empty map for null input");
    }

    // Category 9: Special Characters and Formatting

    @Test
    @Order(80)
    @DisplayName("Test 9.1: Text with exclamation marks")
    void testExclamationMarks() {
        String result = analyzer.analyze("Amazing!!! Incredible!!! Wow!!!");
        assertNotNull(result, "Should handle multiple exclamation marks");
    }

    @Test
    @Order(81)
    @DisplayName("Test 9.2: Text with question marks")
    void testQuestionMarks() {
        String result = analyzer.analyze("How could this be so terrible???");
        assertNotNull(result, "Should handle question marks");
    }

    @Test
    @Order(82)
    @DisplayName("Test 9.3: Text with numbers")
    void testWithNumbers() {
        String result = analyzer.analyze("I give this product 5 out of 5 stars because it is excellent.");
        assertEquals("positive", result, "Should handle text with numbers");
    }

    @Test
    @Order(83)
    @DisplayName("Test 9.4: Long text")
    void testLongText() {
        String longText = "This product exceeded all my expectations. " +
            "The quality is outstanding and the price was very reasonable. " +
            "I would highly recommend this to anyone looking for a great experience. " +
            "The customer service was also excellent and very helpful.";

        String result = analyzer.analyze(longText);
        assertEquals("positive", result, "Should handle long positive text");
    }
}

