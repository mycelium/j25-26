package org.example;

public class MovieReview {
    private String processedText;
    private String actualSentiment;
    private String predictedSentiment;
    
    public MovieReview(String processedText, String actualSentiment) {
        this.processedText = processedText;
        this.actualSentiment = actualSentiment;
        this.predictedSentiment = "Not analyzed yet";
    }
    
    public String getProcessedText() { return processedText; }
    public String getActualSentiment() { return actualSentiment; }
    public String getCalculatedSentiment() { return predictedSentiment; }
    
    public void setCalculatedSentiment(String predictedSentiment) {
        this.predictedSentiment = predictedSentiment;
    }
    
    public boolean isCalculationCorrect() {
        return actualSentiment.equalsIgnoreCase(predictedSentiment);
    }
}