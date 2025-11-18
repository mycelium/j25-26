package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CSVProcessor
{
    /**
     * Adjusts the review format for analysis and analyzes its tone
     */
    public void processCSV(String inputFilePath, String outputFilePath,
                           SentimentAnalyzer sentimentAnalyzer) throws IOException

    {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
             FileWriter fileWriter = new FileWriter(outputFilePath))
        {

            // Skipping the title
            String header = bufferedReader.readLine();
            if (header == null)
            {
                throw new IOException("Input file is empty");
            }

            // Writing a new header to the output file
            fileWriter.write("review,predicted_sentiment\n");

            String currentLine;
            System.out.println("Starting sentiment analysis for non-standard CSV format...");

            while ((currentLine = bufferedReader.readLine()) != null)
            {

                if (currentLine.trim().isEmpty())
                {
                    continue;
                }

                String[] parts = extractReviewAndSentiment(currentLine);

                if (parts != null)
                {
                    // Getting a review
                    String originalReview = parts[0];

                    // We don't use evaluation for analysis, only for verification
                    String originalSentiment = parts[1];

                    // Text cleaning: remove HTML tags and extra spaces
                    String cleanedReview = cleanText(originalReview);

                    // SentimentAnalyzer was used to get a sentiment score
                    String predictedSentiment = sentimentAnalyzer.analyzeSentiment(cleanedReview);

                    // Writing review and predicted sentiment to the output file
                    fileWriter.write(cleanedReview + "," + predictedSentiment + "\n");
                }
            }
            System.out.println("Results saved to: " + outputFilePath);
        }
    }

    /**
     * Extracts the review text and rating from a non-standard CSV line
     */
    private String[] extractReviewAndSentiment(String line)
    {
        if (line == null || line.trim().isEmpty())
        {
            return null;
        }

        // Looking for the last comma in the line, which is the separator between the review and sentiment
        int lastCommaIndex = line.lastIndexOf(',');

        if (lastCommaIndex == -1)
        {
            System.out.println("Warning: No comma found in line: " + line.substring(0, Math.min(50, line.length())));
            return null;
        }

        String review = line.substring(0, lastCommaIndex).trim();

        // Extract the sentiment (everything after the last comma)
        String sentiment = line.substring(lastCommaIndex + 1).trim();

        // Checking that the sentiment is valid
        if (!sentiment.equals("positive") && !sentiment.equals("negative") && !sentiment.equals("neutral"))
        {
            System.out.println("Warning: Invalid sentiment " + sentiment + " in line");
        }

        return new String[]{review, sentiment};
    }

    /**
     * Cleans the text from HTML tags and extra spaces
     * Removes the <br /><br /> tags that appear in the .csv file
     */
    private String cleanText(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            return text;
        }

        // Removing HTML tags: replace <br /><br /> with a space
        String cleanedText = text.replaceAll("<br /><br />", " ");
        cleanedText = cleanedText.replaceAll("<br />", " ");
        cleanedText = cleanedText.replaceAll("<br>", " ");

        // Remove any extra spaces that may have appeared after removing the tags
        cleanedText = cleanedText.replaceAll("\\s+", " ");
        cleanedText = cleanedText.trim();

        return cleanedText;
    }

    public void checkAnalyzeSentimentResult(String inputFilePath, String outputFilePath) throws IOException
    {
        try (BufferedReader bufferedReaderInput = new BufferedReader(new FileReader(inputFilePath));
             BufferedReader bufferedReaderOutput = new BufferedReader(new FileReader(outputFilePath)))
        {

            String headerInput = bufferedReaderInput.readLine();
            String headerOutput = bufferedReaderOutput.readLine();

            if (headerInput == null || headerOutput == null)
            {
                throw new IOException("Input or Output file is empty");
            }

            String currentLineInput;
            String currentLineOutput;

            int totalReview = 0;
            int correctReview = 0;

            while ((currentLineInput = bufferedReaderInput.readLine()) != null
                    && (currentLineOutput = bufferedReaderOutput.readLine()) != null)
            {
                if (currentLineInput.trim().isEmpty() || currentLineOutput.trim().isEmpty())
                {
                    continue;
                }

                String[] partsInput = extractReviewAndSentiment(currentLineInput);
                String[] partsOutput = extractReviewAndSentiment(currentLineOutput);

                if (partsInput != null && partsOutput != null)
                {
                    String originalSentiment = partsInput[1];
                    String predictedSentiment = partsOutput[1];
                    if (originalSentiment.equals(predictedSentiment)) correctReview++;
                }
                totalReview++;
            }
            System.out.println("Proportion of correctly predicted sentiments: " + correctReview  + "/" + totalReview);
        }
    }
}