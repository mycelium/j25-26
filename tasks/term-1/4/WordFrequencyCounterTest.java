package org.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;



@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WordFrequencyCounterTest {

    @TempDir
    Path tempDir;

    private WordFrequencyCounter counter;

    @BeforeEach
    void setUp() {
        counter = new WordFrequencyCounter();
    }

    // Category 1: Basic Functionality

    @Test
    @Order(1)
    @DisplayName("Test 1.1: Empty file returns empty map")
    void testEmptyFile() throws IOException {
        Path file = createTestFile("empty.txt", "");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Empty file should return empty map");
    }

    @Test
    @Order(2)
    @DisplayName("Test 1.2: Single word once")
    void testSingleWordOnce() throws IOException {
        Path file = createTestFile("single.txt", "hello");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should have 1 unique word");
        assertEquals(1, result.get("hello"), "Word 'hello' should appear once");
    }

    @Test
    @Order(3)
    @DisplayName("Test 1.3: Single word multiple times")
    void testSingleWordMultipleTimes() throws IOException {
        Path file = createTestFile("repeated.txt", "hello hello hello");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should have 1 unique word");
        assertEquals(3, result.get("hello"), "Word 'hello' should appear 3 times");
    }

    @Test
    @Order(4)
    @DisplayName("Test 1.4: Multiple different words")
    void testMultipleDifferentWords() throws IOException {
        Path file = createTestFile("multiple.txt", "hello world foo bar");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(4, result.size(), "Should have 4 unique words");
        assertEquals(1, result.get("hello"));
        assertEquals(1, result.get("world"));
        assertEquals(1, result.get("foo"));
        assertEquals(1, result.get("bar"));
    }

    @Test
    @Order(5)
    @DisplayName("Test 1.5: Mixed frequencies")
    void testMixedFrequencies() throws IOException {
        Path file = createTestFile("mixed.txt", "apple banana apple cherry banana apple");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Should have 3 unique words");
        assertEquals(3, result.get("apple"), "Word 'apple' should appear 3 times");
        assertEquals(2, result.get("banana"), "Word 'banana' should appear 2 times");
        assertEquals(1, result.get("cherry"), "Word 'cherry' should appear once");
    }

    // Category 2: Word Definition & Boundaries

    @Test
    @Order(10)
    @DisplayName("Test 2.1: Case insensitivity")
    void testCaseInsensitivity() throws IOException {
        Path file = createTestFile("case.txt", "Hello HELLO hello HeLLo");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "All variations should be counted as same word");
        assertEquals(4, result.get("hello"), "All case variations should count as 'hello'");
    }

    @Test
    @Order(11)
    @DisplayName("Test 2.2: Multiple spaces between words")
    void testMultipleSpaces() throws IOException {
        Path file = createTestFile("spaces.txt", "word    word");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should have 1 unique word");
        assertEquals(2, result.get("word"), "Multiple spaces should be treated as single separator");
    }

    @Test
    @Order(12)
    @DisplayName("Test 2.3: Tabs and newlines as separators")
    void testTabsAndNewlines() throws IOException {
        Path file = createTestFile("whitespace.txt", "word\tword\nword");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should have 1 unique word");
        assertEquals(3, result.get("word"), "Tabs and newlines should separate words");
    }

    @Test
    @Order(13)
    @DisplayName("Test 2.4: Leading and trailing whitespace")
    void testLeadingTrailingWhitespace() throws IOException {
        Path file = createTestFile("trim.txt", "   word   ");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should have 1 unique word");
        assertEquals(1, result.get("word"), "Leading/trailing whitespace should be ignored");
    }

    @Test
    @Order(14)
    @DisplayName("Test 2.5: Words with numbers and symbols")
    void testWordsWithNumbersAndSymbols() throws IOException {
        Path file = createTestFile("symbols.txt", "test123 hello@world price$50");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Should have 3 unique words");
        assertTrue(result.containsKey("test123"), "Should contain 'test123'");
        assertTrue(result.containsKey("hello@world"), "Should contain 'hello@world'");
        assertTrue(result.containsKey("price$50"), "Should contain 'price$50'");
    }

    @Test
    @Order(15)
    @DisplayName("Test 2.6: File with only whitespace")
    void testOnlyWhitespace() throws IOException {
        Path file = createTestFile("onlyspace.txt", "   \t\n   ");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "File with only whitespace should return empty map");
    }

    // Category 3: Multilingual Support

    @Test
    @Order(20)
    @DisplayName("Test 3.1: Cyrillic text (Russian)")
    void testCyrillicText() throws IOException {
        Path file = createTestFile("russian.txt", "привет мир привет");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should have 2 unique words");
        assertEquals(2, result.get("привет"), "Russian word 'привет' should appear twice");
        assertEquals(1, result.get("мир"), "Russian word 'мир' should appear once");
    }

    @Test
    @Order(21)
    @DisplayName("Test 3.2: Mixed languages")
    void testMixedLanguages() throws IOException {
        Path file = createTestFile("mixed_lang.txt", "hello мир hello world");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Should have 3 unique words");
        assertEquals(2, result.get("hello"));
        assertEquals(1, result.get("мир"));
        assertEquals(1, result.get("world"));
    }

    @Test
    @Order(22)
    @DisplayName("Test 3.3: Chinese characters")
    void testChineseCharacters() throws IOException {
        Path file = createTestFile("chinese.txt", "你好 世界 你好");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should have 2 unique words");
        assertEquals(2, result.get("你好"), "Chinese word should appear twice");
        assertEquals(1, result.get("世界"), "Chinese word should appear once");
    }

    @Test
    @Order(23)
    @DisplayName("Test 3.4: Accented characters")
    void testAccentedCharacters() throws IOException {
        Path file = createTestFile("accents.txt", "café naïve café");
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should have 2 unique words");
        assertEquals(2, result.get("café"), "Word with accent should appear twice");
        assertEquals(1, result.get("naïve"), "Word with diaeresis should appear once");
    }

    // Category 4: File Size & Performance

    // Важное примечание: здесь то, что считается маленькими, средними и большими файлами, является лишь примером,
    // они могут быть больше или меньше. Для моей конкретной лаборатории я тестировал текстовые файлы размером до 10 ГБ.

    @Test
    @Order(30)
    @DisplayName("Test 4.1: Small file (< 1 MB)")
    @Timeout(10)
    void testSmallFile() throws IOException {
        // Create ~100 KB file
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            content.append("word").append(i % 100).append(" ");
        }
        
        Path file = createTestFile("small.txt", content.toString());
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() <= 100, "Should have at most 100 unique words");
        
        int totalCount = result.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(10000, totalCount, "Total word count should be 10000");
    }

    @Test
    @Order(31)
    @DisplayName("Test 4.2: Medium file (~10 MB)")
    @Timeout(30)
    void testMediumFile() throws IOException {
        // Create ~10 MB file
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 1000000; i++) {
            content.append("word").append(i % 1000).append(" ");
        }
        
        Path file = createTestFile("medium.txt", content.toString());
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() <= 1000, "Should have at most 1000 unique words");
        
        int totalCount = result.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(1000000, totalCount, "Total word count should be 1000000");
    }

    @Test
    @Order(32)
    @DisplayName("Test 4.3: Large file (> 500 MB) -  STREAMING TEST")
    @Timeout(300) // 5 minutes max
    void testLargeFileStreaming() throws IOException {
        System.out.println("Starting large file test (> 500 MB)...");
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        // Create a 600 MB file
        Path file = createLargeTestFile("large.txt", 600);
        
        System.out.println("Large file created, starting processing...");
        Map<String, Integer> result = counter.countWords(file);
        
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryUsed = (endMemory - startMemory) / (1024 * 1024); // MB
        
        System.out.println("Processing completed. Memory used: ~" + memoryUsed + " MB");
        
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Result should not be empty");
        
        assertTrue(result.containsKey("word0") || result.containsKey("word1"),
                   "Should contain expected words");
        
        System.out.println("  TEST PASSED: Large file processed without OutOfMemoryError");
    }

    @Test
    @Order(33)
    @DisplayName("Test 4.4: Very large file (> 1 GB) ")
    @Timeout(600)
    void testVeryLargeFile() throws IOException {
        System.out.println("Starting very large file test (> 1 GB)...");
        
        // Create a 1.5 GB file
        Path file = createLargeTestFile("verylarge.txt", 1500);
        
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Result should not be empty");
        
        System.out.println(" Very large file test passed");
    }

    // Category 5: Edge Cases

    @Test
    @Order(40)
    @DisplayName("Test 5.1: Very long line (no newlines)")
    @Timeout(60)
    void testVeryLongLine() throws IOException {
        // Create 10 MB single line
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 1000000; i++) {
            content.append("word").append(i % 100).append(" ");
        }
        
        Path file = createTestFile("longline.txt", content.toString());
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Should process long line without error");
        
        int totalCount = result.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(1000000, totalCount, "Total word count should be correct");
    }

    @Test
    @Order(41)
    @Timeout(180)
    @DisplayName("Test 5.2: Very long word (continuous characters)")
    void testVeryLongWord() throws IOException {
        // Create 1GB continuous word
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < 1024 * 1024 * 1024; i++) {
            word.append('a');
        }
        
        Path file = createTestFile("longword.txt", word.toString());
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Should handle very long word gracefully");
        
        System.out.println("Long word handled: " + result.size() + " chunk(s)");
    }

    @Test
    @Order(42)
    @DisplayName("Test 5.3: Many unique words")
    @Timeout(60)
    void testManyUniqueWords() throws IOException {
        // Create file with 100K unique words
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            content.append("word").append(i).append(" ");
        }
        
        Path file = createTestFile("manyunique.txt", content.toString());
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(100000, result.size(), "Should have 100000 unique words");
        
        assertTrue(result.values().stream().allMatch(count -> count == 1),
                   "All words should appear exactly once");
    }

    @Test
    @Order(43)
    @DisplayName("Test 5.4: Few words, high frequency")
    @Timeout(30)
    void testHighFrequency() throws IOException {
        // Create file with one word repeated many times
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 1000000; i++) {
            content.append("word ");
        }
        
        Path file = createTestFile("highfreq.txt", content.toString());
        Map<String, Integer> result = counter.countWords(file);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should have 1 unique word");
        assertEquals(1000000, result.get("word"), "Word should appear 1 million times");
    }

    // Category 6: Error Handling

    @Test
    @Order(50)
    @DisplayName("Test 6.1: Non-existent file")
    void testNonExistentFile() {
        Path file = tempDir.resolve("nonexistent.txt");
        
        assertDoesNotThrow(() -> {
            Map<String, Integer> result = counter.countWords(file);
            assertNotNull(result, "Should return non-null result even for non-existent file");
            assertTrue(result.isEmpty(), "Should return empty map for non-existent file");
        }, "Should handle non-existent file gracefully");
    }

    @Test
    @Order(51)
    @DisplayName("Test 6.2: Null path")
    void testNullPath() {
        assertDoesNotThrow(() -> {
            Map<String, Integer> result = counter.countWords(null);
            assertNotNull(result, "Should return non-null result for null path");
            assertTrue(result.isEmpty(), "Should return empty map for null path");
        }, "Should handle null path gracefully without NPE");
    }

    @Test
    @Order(52)
    @DisplayName("Test 6.3: Directory instead of file")
    void testDirectory() {
        Path dir = tempDir.resolve("testdir");
        
        assertDoesNotThrow(() -> {
            Files.createDirectory(dir);
            Map<String, Integer> result = counter.countWords(dir);
            assertNotNull(result, "Should return non-null result for directory");
            assertTrue(result.isEmpty(), "Should return empty map for directory");
        }, "Should handle directory gracefully");
    }

    // Helper Methods

    private Path createTestFile(String filename, String content) throws IOException {
        Path file = tempDir.resolve(filename);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private Path createLargeTestFile(String filename, int sizeMB) throws IOException {
        Path file = tempDir.resolve(filename);
        
        StringBuilder chunk = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            chunk.append("word").append(i % 100).append(" ");
        }
        String chunkStr = chunk.toString();
        
        long chunkSize = chunkStr.getBytes(StandardCharsets.UTF_8).length;
        long targetSize = (long) sizeMB * 1024 * 1024;
        long chunksNeeded = targetSize / chunkSize;
        
        System.out.println("Creating " + sizeMB + " MB file with " + chunksNeeded + " chunks...");
        
        for (long i = 0; i < chunksNeeded; i++) {
            Files.writeString(file, chunkStr, StandardCharsets.UTF_8, 
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.APPEND);
            
            if (i % 100 == 0) {
                System.out.print(".");
            }
        }
        System.out.println("\nFile created: " + Files.size(file) / (1024 * 1024) + " MB");
        
        return file;
    }
}