package org.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WordFrequencyCounter {

    private static final long MAX_FILE_SIZE_FOR_FULL_LOAD = 500 * 1024 * 1024;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final int MAX_WORD_LENGTH = 1024 * 1024;

    private boolean validateFilePath(Path filePath) {
        if (filePath == null) {
            System.err.println("Error: File path cannot be null");
            return false;
        }

        if (!Files.exists(filePath)) {
            System.err.println("Error: File does not exist - " + filePath);
            return false;
        }

        if (!Files.isRegularFile(filePath)) {
            System.err.println("Error: Path is not a regular file - " + filePath);
            return false;
        }

        if (!Files.isReadable(filePath)) {
            System.err.println("Error: File is not readable - " + filePath);
            return false;
        }

        try {
            long fileSize = Files.size(filePath);
            if (fileSize == 0) {
                System.out.println("Warning: File is empty - " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error: Cannot determine file size - " + e.getMessage());
            return false;
        }

        return true;
    }

    private String processWord(String word) {
        if (word == null || word.isEmpty()) {
            return null;
        }

        word = word.trim();

        return word;
    }

    public Map<String, Integer> countWords(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();

        if (!validateFilePath(filePath)) {
            return wordCount;
        }

        try {
            long fileSize = Files.size(filePath);
            
            if (fileSize > MAX_FILE_SIZE_FOR_FULL_LOAD) {
                System.out.println("File is large (" + (fileSize / (1024 * 1024)) + " MB). Using streaming method...");
                return countWordsStreaming(filePath);
            }
            
            System.out.println("Loading entire file into memory...");

            String content = Files.readString(filePath, DEFAULT_CHARSET);

            if (content == null || content.isEmpty()) {
                System.out.println("File is empty or contains no readable content.");
                return wordCount;
            }

            String[] words = content.toLowerCase().split("\\s+");

            for (String word : words) {
                if (word.length() > MAX_WORD_LENGTH) {
                    System.out.println("Processing extremely long word (" + (word.length() / (1024 * 1024)) + " MB) in chunks...");
                    int start = 0;
                    while (start < word.length()) {
                        int end = Math.min(start + MAX_WORD_LENGTH, word.length());
                        String chunk = word.substring(start, end);
                        String processedChunk = processWord(chunk);
                        if (processedChunk != null && !processedChunk.isEmpty()) {
                            wordCount.merge(processedChunk, 1, Integer::sum);
                        }
                        start = end;
                    }
                } else {
                    String processedWord = processWord(word);
                    if (processedWord != null && !processedWord.isEmpty()) {
                        wordCount.merge(processedWord, 1, Integer::sum);
                    }
                }
            }

            System.out.println("Successfully processed file.");

        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory! Switching to streaming method...");
            wordCount.clear();
            return countWordsStreaming(filePath);
        } catch (AccessDeniedException e) {
            System.err.println("Access denied: No permission to read file - " + filePath);
        } catch (IOException e) {
            System.err.println("IO Error reading file: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        return wordCount;
    }


    private Map<String, Integer> countWordsStreaming(Path filePath) {
        Map<String, Integer> wordCount = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath, DEFAULT_CHARSET)) {
            System.out.println("Streaming file processing (chunk by chunk)...");

            char[] buffer = new char[8192];
            int charsRead;
            StringBuilder currentWord = new StringBuilder();
            long totalCharsProcessed = 0;
            long lastProgressReport = 0;

            while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                totalCharsProcessed += charsRead;

                if (totalCharsProcessed - lastProgressReport > 100 * 1024 * 1024) {
                    System.out.println("Processed " + (totalCharsProcessed / (1024 * 1024)) + " MB...");
                    lastProgressReport = totalCharsProcessed;
                }

                for (int i = 0; i < charsRead; i++) {
                    char c = buffer[i];

                    if (!Character.isWhitespace(c)) {
                        currentWord.append(Character.toLowerCase(c));
                        
                        if (currentWord.length() >= MAX_WORD_LENGTH) {
                            String word = currentWord.toString();
                            String processedWord = processWord(word);
                            if (processedWord != null && !processedWord.isEmpty()) {
                                wordCount.merge(processedWord, 1, Integer::sum);
                            }
                            currentWord.setLength(0);
                        }
                    } else {
                        if (currentWord.length() > 0) {
                            String word = currentWord.toString();
                            String processedWord = processWord(word);
                            if (processedWord != null && !processedWord.isEmpty()) {
                                wordCount.merge(processedWord, 1, Integer::sum);
                            }
                            currentWord.setLength(0);
                        }
                    }
                }
            }

            if (currentWord.length() > 0) {
                String word = currentWord.toString();
                String processedWord = processWord(word);
                if (processedWord != null && !processedWord.isEmpty()) {
                    wordCount.merge(processedWord, 1, Integer::sum);
                }
            }

            System.out.println("Successfully processed " + (totalCharsProcessed / (1024 * 1024)) + " MB.");

        } catch (AccessDeniedException e) {
            System.err.println("Access denied: No permission to read file - " + filePath);
        } catch (IOException e) {
            System.err.println("IO Error reading file: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        return wordCount;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        if (frequencies == null) {
            System.err.println("Error: Cannot print null frequencies map");
            return;
        }

        if (frequencies.isEmpty()) {
            System.out.println("No words found.");
            return;
        }

        try {
            System.out.println("\nWord Frequency Results:");
            System.out.println("================");

            long totalWords = 0;
            for (Integer count : frequencies.values()) {
                if (count != null && count > 0) {
                    totalWords += count;
                }
            }

            System.out.println("Total words: " + totalWords);
            System.out.println("Unique words: " + frequencies.size());
            
            System.out.println("\nWord Counts:");
            System.out.println("------------");
            for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

        } catch (Exception e) {
            System.err.println("Error printing frequencies: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            WordFrequencyCounter counter = new WordFrequencyCounter();

            Path filePath = Path.of("C:/Users/pc/PyCharmMiscProject/1GB.txt");
            System.out.println("Processing file: " + filePath);
            System.out.println("===========================================\n");

            Map<String, Integer> frequencies = counter.countWords(filePath);
            
            if (frequencies != null && !frequencies.isEmpty()) {
                counter.printFrequencies(frequencies);
            }

        } catch (Exception e) {
            System.err.println("Fatal error in main: " + e.getMessage());
            e.printStackTrace();
        }
    }
}