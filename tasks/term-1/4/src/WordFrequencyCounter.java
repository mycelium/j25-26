import java.util.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;

public class WordFrequencyCounter
{
    // mode 1: downloading the entire file content and then processing it
    public static Map<String, Integer> countWords(Path filePath)
    {
        // check the existence of the file and the readability
        if (!Files.exists(filePath) || !Files.isReadable(filePath))
        {
            throw new RuntimeException("The file does not exist or is not readable.");
        }

        try
        {
            List<String> lines = Files.readAllLines(filePath);
            Map<String, Integer> wordCount = new HashMap<>();

            Pattern wordPattern = Pattern.compile("[\\p{L}\\d]+(?:[\\-'+/=*.\"][\\p{L}\\d]+)*");

            for (String line : lines)
            {
                Matcher matcher = wordPattern.matcher(line);

                while (matcher.find())
                {
                    String word = matcher.group();
                    if (!word.isEmpty())
                    {
                        String wordInLowerCase = word.toLowerCase();
                        wordCount.put(wordInLowerCase, wordCount.getOrDefault(wordInLowerCase, 0) + 1);
                    }
                }
            }
            return wordCount;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error reading the file", e);
        }
    }

    // mode 2: streaming file processing (for large files)
    public static Map<String, Integer> countWordsStreaming(Path filePath)
    {
        // check the existence of the file and the readability
        if (!Files.exists(filePath) || !Files.isReadable(filePath))
        {
            throw new RuntimeException("The file does not exist or is not readable.");
        }

        Map<String, Integer> wordCount = new HashMap<>();
        Pattern wordPattern = Pattern.compile("[\\p{L}\\d]+(?:[\\-'+/=*.\"][\\p{L}\\d]+)*");

        try (BufferedReader reader = Files.newBufferedReader(filePath))
        {
            String line;
            int lineNumber = 0;

            // single line streaming reading
            while ((line = reader.readLine()) != null)
            {
                lineNumber++;
                Matcher matcher = wordPattern.matcher(line);

                while (matcher.find())
                {
                    String word = matcher.group();
                    if (!word.isEmpty())
                    {
                        String wordInLowerCase = word.toLowerCase();
                        wordCount.put(wordInLowerCase, wordCount.getOrDefault(wordInLowerCase, 0) + 1);
                    }
                }

                // progress for large files
                if (lineNumber % 1000 == 0)
                {
                    System.out.printf("Processed %d lines...%n", lineNumber);
                }
            }
            System.out.printf("Total lines processed: %d%n", lineNumber);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error reading the file", e);
        }

        return wordCount;
    }

    public static void printFrequencies(Map<String, Integer> frequencies)
    {
        if (frequencies.isEmpty())
        {
            System.out.println("Map with word frequency counter is empty");
            return;
        }

        System.out.println("-" .repeat(40));
        System.out.printf("%-20s %-10s%n", "WORD", "COUNT");
        System.out.println("-" .repeat(40));

        // sort by descending frequency of words
        frequencies.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf("%-20s %-10d%n", entry.getKey(), entry.getValue()));

        System.out.println("-" .repeat(40));
        System.out.println("Total words: " + frequencies.size());
    }

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Provide the path to the text file as a command line argument");
            return;
        }

        try
        {
            Path filePath = Path.of(args[0]);
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long fileSize = Files.size(filePath);

            System.out.println("\n" + "=" .repeat(40));
            System.out.println("FILE PROCESSING INFO");
            System.out.println("=" .repeat(40));

            System.out.printf("File path: %s%n", filePath);
            System.out.printf("File size: %.2f KB%n", fileSize / 1024.0);
            System.out.printf("Max JVM memory: %.2f KB%n", maxMemory / 1024.0);
            System.out.println("\n");

            Map<String, Integer> wordFrequencies;

            if (fileSize > maxMemory * 0.7)
            {
                System.out.println("USING STREAMING (file is larger than 70% of available memory)");
                wordFrequencies = countWordsStreaming(filePath);
            }
            else
            {
                System.out.println("USING FULL LOAD (file fits in memory)");
                wordFrequencies = countWords(filePath);
            }
            printFrequencies(wordFrequencies);
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }
}