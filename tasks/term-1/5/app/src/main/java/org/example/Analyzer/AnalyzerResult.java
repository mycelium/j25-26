package org.example.Analyzer;

class AnalyzerResult 
{
    private String sentiment;
    private int sentimentScore;
    private String originalText;

    public AnalyzerResult(
        String sentiment,
        int score,
        String text
    ){
        this.sentiment = sentiment;
        sentimentScore = score;
        originalText = text;
    }

    public String getSentiment() { return sentiment; }
    public int getSentimentScore() { return sentimentScore; }
    public String getOriginalText() { return originalText; }
    
    @Override
    public String toString() {
        return String.format("Text: %s\nSentiment: %s (Score: %d)", 
            originalText.length() > 50 ? originalText.substring(0, 50) + "..." : originalText,
            sentiment, sentimentScore);
    }
}
