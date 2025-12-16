package org.example;

import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.util.*;

public class App {

    private static int iterationsNumb = 20;

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) throws IOException, CsvValidationException {
        try {
            DataSetReader     dsr         = new DataSetReader("IMDB Dataset.csv");
            SentimentPipeline sp          = new SentimentPipeline();
            int               correctNumb = 0;

            List<Review> reviews = dsr.readAll();
            dsr.close();

            for (int i = 1; i <= iterationsNumb; i++) {
                int    rInd        = ((int) (Math.random() * (reviews.size() - 1)));
                String reviewText  = reviews.get(rInd).revText();
                String reviewGrade = reviews.get(rInd).revGrade();
                String sentGrade   = sp.analyzeReviewText(reviewText);

                if (sentGrade.equals(reviewGrade)) correctNumb++;

                System.out.printf("""
                        Review number %d | %s
                        Actual rev.   : %s
                        Sentiment rev.: %s
                        ---------------------
                        """, i,
                            (reviewText).length() > 50
                                    ? reviewText.substring(0, 50) + "..."
                                    : reviewText,
                             reviewGrade,
                             sentGrade);
                reviews.remove(rInd);
            }
            System.out.println("Result:\n Percent of correctly predicted reviews: " +
                               (correctNumb / (((double) iterationsNumb )/ 100)));
        }
        catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
