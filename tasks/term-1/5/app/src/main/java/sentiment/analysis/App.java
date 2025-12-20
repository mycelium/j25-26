package sentiment.analysis;

import java.util.List;

public class App {
    private SentimentAnalyzer sentimentAnalyzer;
    private DatasetProcessor datasetProcessor;

    public App() {
        System.out.println("Initializing Sentiment Analyzer...");
        this.sentimentAnalyzer = new SentimentAnalyzer();
        this.datasetProcessor = new DatasetProcessor();
    }

    public void analyzeMovieReviews() {
        try {
            String inputPath = "../IMDB Dataset.csv";

            System.out.println("Reading reviews from: " + inputPath);
            List<MovieReview> reviews = datasetProcessor.readReviewFromDataset(inputPath);

            if(reviews.isEmpty()) {
                System.out.println("No reviews found in the dataset file.");
                return;
            }

            System.out.println("Found " + reviews.size() + " reviews to analyze.");

            // ограничиваем количество отзывов для тестирования, а то 50000 долго очень...
            int testLimit = 10;
            if (reviews.size() > testLimit) {
                System.out.println("Limiting analysis to first " + testLimit + " reviews...");
                reviews = reviews.subList(0, testLimit);
            }

            System.out.println("Starting sentiment analysis...");
            for (int i = 0; i < reviews.size(); i++) {
                MovieReview review = reviews.get(i);
                MovieReview analyzedReview = sentimentAnalyzer.analyzeSentiment(review.getReviewText());
                review.setSentiment(analyzedReview.getSentiment());

                // каждые 10 отзывов выводим процесс обработки
                // не особо нужно при обработке 10 отзывов в целом, но...
                if ((i + 1) % 10 == 0) {
                    System.out.println("Processed " + (i + 1) + "/" + reviews.size() + " reviews...");
                }
            }

            System.out.println("Analysis completed!");

            datasetProcessor.printResults(reviews);

        } catch (Exception e) {
            System.err.println("Error during analysis: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (sentimentAnalyzer != null) {
                sentimentAnalyzer.close();
            }
        }
    }

    public static void main(String[] args) {
        App app = new App();

        System.out.println("Movie Review Sentiment Analysis");
        System.out.println("===============================");

        System.out.println("\nStarting analysis...");

        app.analyzeMovieReviews();

        System.exit(0);
    }
}