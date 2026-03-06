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

    private void assertSentiment(String input, String expected) {
        String result = analyzer.analyze(input);
        assertEquals(expected, result,
                "analyze(\"" + input + "\") should return '" + expected + "', but got '" + result + "'");
    }

    @Test
    @Order(1)
    @DisplayName("Test 1.1: Simple positive sentence")
    void testSimplePositive() {
        assertSentiment("I love this product!", "positive");
    }

    @Test
    @Order(2)
    @DisplayName("Test 1.2: Very positive sentence")
    void testVeryPositive() {
        assertSentiment("This is absolutely amazing and wonderful!", "positive");
    }

    @Test
    @Order(3)
    @DisplayName("Test 1.3: Positive with enthusiasm")
    void testPositiveEnthusiasm() {
        assertSentiment("Great job! Excellent work! I am so happy!", "positive");
    }

    @Test
    @Order(4)
    @DisplayName("Test 1.4: Positive review")
    void testPositiveReview() {
        assertSentiment("The movie was fantastic and the actors were brilliant.", "positive");
    }

    @Test
    @Order(5)
    @DisplayName("Test 1.5: Positive with superlatives")
    void testPositiveSuperlatives() {
        assertSentiment("This is the best experience I have ever had.", "positive");
    }

    // Category 2: Negative Sentiment

    @Test
    @Order(10)
    @DisplayName("Test 2.1: Simple negative sentence")
    void testSimpleNegative() {
        assertSentiment("I hate this product.", "negative");
    }

    @Test
    @Order(11)
    @DisplayName("Test 2.2: Very negative sentence")
    void testVeryNegative() {
        assertSentiment("This is terrible and awful!", "negative");
    }

    @Test
    @Order(12)
    @DisplayName("Test 2.3: Negative complaint")
    void testNegativeComplaint() {
        assertSentiment("The service was horrible. I am very disappointed.", "negative");
    }

    @Test
    @Order(13)
    @DisplayName("Test 2.4: Negative review")
    void testNegativeReview() {
        assertSentiment("The movie was boring and the plot was confusing.", "negative");
    }

    @Test
    @Order(14)
    @DisplayName("Test 2.5: Strong negative words")
    void testStrongNegative() {
        assertSentiment("This is the worst thing I have ever seen.", "negative");
    }

    // Category 3: Neutral Sentiment

    @Test
    @Order(20)
    @DisplayName("Test 3.1: Factual statement")
    void testFactualStatement() {
        assertSentiment("The meeting is scheduled for tomorrow.", "neutral");
    }

    @Test
    @Order(21)
    @DisplayName("Test 3.2: Simple description")
    void testSimpleDescription() {
        assertSentiment("The book has 300 pages.", "neutral");
    }

    @Test
    @Order(22)
    @DisplayName("Test 3.3: Neutral question")
    void testNeutralQuestion() {
        assertSentiment("What time does the store open?", "neutral");
    }

    // Category 4: Edge Cases - Empty and Null

    @Test
    @Order(30)
    @DisplayName("Test 4.1: Null input")
    void testNullInput() {
        String result = analyzer.analyze(null);
        assertEquals("neutral", result,
                "analyze(null) should return 'neutral' for null input, but got '" + result + "'. "
                        + "Null input must be handled gracefully without throwing exceptions.");
    }

    @Test
    @Order(31)
    @DisplayName("Test 4.2: Empty string")
    void testEmptyString() {
        String result = analyzer.analyze("");
        assertEquals("neutral", result,
                "analyze(\"\") should return 'neutral' for empty string, but got '" + result + "'. "
                        + "Empty input must be handled gracefully.");
    }

    @Test
    @Order(32)
    @DisplayName("Test 4.3: Whitespace only")
    void testWhitespaceOnly() {
        String result = analyzer.analyze("   \t\n   ");
        assertEquals("neutral", result,
                "analyze(\"   \\t\\n   \") should return 'neutral' for whitespace-only input, but got '" + result + "'. "
                        + "Whitespace-only input must be treated as empty.");
    }

    @Test
    @Order(33)
    @DisplayName("Test 4.4: Single word positive")
    void testSingleWordPositive() {
        String result = analyzer.analyze("Amazing!");
        assertNotNull(result,
                "analyze(\"Amazing!\") returned null. Single-word input must return a non-null sentiment.");
    }

    @Test
    @Order(34)
    @DisplayName("Test 4.5: Single word negative")
    void testSingleWordNegative() {
        String result = analyzer.analyze("Terrible!");
        assertNotNull(result,
                "analyze(\"Terrible!\") returned null. Single-word input must return a non-null sentiment.");
    }

    // Category 5: Multiple Sentences

    @Test
    @Order(40)
    @DisplayName("Test 5.1: Multiple positive sentences")
    void testMultiplePositiveSentences() {
        assertSentiment("I love this. It is amazing. Best purchase ever.", "positive");
    }

    @Test
    @Order(41)
    @DisplayName("Test 5.2: Multiple negative sentences")
    void testMultipleNegativeSentences() {
        assertSentiment("This is awful and terrible. I absolutely hate it.", "negative");
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
        String input = "I absolutely love this!";
        String result = analyzer.analyzeDetailed(input);
        assertTrue(
            result.equals("Positive") || result.equals("Very positive"),
            "analyzeDetailed(\"" + input + "\") should return 'Positive' or 'Very positive', "
                    + "but got '" + result + "'"
        );
    }

    @Test
    @Order(51)
    @DisplayName("Test 6.2: Detailed negative analysis")
    void testDetailedNegative() {
        String input = "I absolutely hate this!";
        String result = analyzer.analyzeDetailed(input);
        assertTrue(
            result.equals("Negative") || result.equals("Very negative"),
            "analyzeDetailed(\"" + input + "\") should return 'Negative' or 'Very negative', "
                    + "but got '" + result + "'"
        );
    }

    @Test
    @Order(52)
    @DisplayName("Test 6.3: Detailed neutral analysis")
    void testDetailedNeutral() {
        String input = "The weather is cloudy today.";
        String result = analyzer.analyzeDetailed(input);
        assertEquals("Neutral", result,
                "analyzeDetailed(\"" + input + "\") should return 'Neutral', but got '" + result + "'");
    }

    // Category 7: Simplify Sentiment Method

    private void assertSimplify(String input, String expected) {
        String result = analyzer.simplifySentiment(input);
        assertEquals(expected, result,
                "simplifySentiment(" + (input == null ? "null" : "\"" + input + "\"") + ") "
                        + "should return '" + expected + "', but got '" + result + "'");
    }

    @Test
    @Order(60)
    @DisplayName("Test 7.1: Simplify 'Very positive' to 'positive'")
    void testSimplifyVeryPositive() {
        assertSimplify("Very positive", "positive");
    }

    @Test
    @Order(61)
    @DisplayName("Test 7.2: Simplify 'Positive' to 'positive'")
    void testSimplifyPositive() {
        assertSimplify("Positive", "positive");
    }

    @Test
    @Order(62)
    @DisplayName("Test 7.3: Simplify 'Very negative' to 'negative'")
    void testSimplifyVeryNegative() {
        assertSimplify("Very negative", "negative");
    }

    @Test
    @Order(63)
    @DisplayName("Test 7.4: Simplify 'Negative' to 'negative'")
    void testSimplifyNegative() {
        assertSimplify("Negative", "negative");
    }

    @Test
    @Order(64)
    @DisplayName("Test 7.5: Simplify 'Neutral' to 'neutral'")
    void testSimplifyNeutral() {
        assertSimplify("Neutral", "neutral");
    }

    @Test
    @Order(65)
    @DisplayName("Test 7.6: Simplify null to 'neutral'")
    void testSimplifyNull() {
        assertSimplify(null, "neutral");
    }

    @Test
    @Order(66)
    @DisplayName("Test 7.7: Simplify unknown to 'neutral'")
    void testSimplifyUnknown() {
        assertSimplify("Unknown", "neutral");
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

        assertEquals(3, results.size(),
                "analyzeBatch() with 3 texts should return 3 results, but got " + results.size()
                        + ". Actual results: " + results);
        for (var entry : Map.of("I love this!", "positive", "I hate this!", "negative", "The sky is blue.", "neutral").entrySet()) {
            assertEquals(entry.getValue(), results.get(entry.getKey()),
                    "analyzeBatch(): text '" + entry.getKey() + "' should be '" + entry.getValue()
                            + "', but got '" + results.get(entry.getKey()) + "'. Full results: " + results);
        }
    }

    @Test
    @Order(71)
    @DisplayName("Test 8.2: Batch analysis with empty list")
    void testBatchAnalysisEmpty() {
        List<String> texts = new ArrayList<>();

        Map<String, String> results = analyzer.analyzeBatch(texts);

        assertNotNull(results,
                "analyzeBatch(emptyList) returned null, expected an empty map");
        assertTrue(results.isEmpty(),
                "analyzeBatch(emptyList) should return an empty map, but got " + results.size()
                        + " entries: " + results);
    }

    @Test
    @Order(72)
    @DisplayName("Test 8.3: Batch analysis with null list")
    void testBatchAnalysisNull() {
        Map<String, String> results = analyzer.analyzeBatch(null);

        assertNotNull(results,
                "analyzeBatch(null) returned null, expected an empty map. "
                        + "Null input must be handled gracefully.");
        assertTrue(results.isEmpty(),
                "analyzeBatch(null) should return an empty map, but got " + results.size()
                        + " entries: " + results);
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
        assertSentiment("I give this product 5 out of 5 stars because it is excellent.", "positive");
    }

    @Test
    @Order(83)
    @DisplayName("Test 9.4: Long text")
    void testLongText() {
        String longText = "This product exceeded all my expectations. " +
            "The quality is outstanding and the price was very reasonable. " +
            "I would highly recommend this to anyone looking for a great experience. " +
            "The customer service was also excellent and very helpful.";

        assertSentiment(longText, "positive");
    }
}

