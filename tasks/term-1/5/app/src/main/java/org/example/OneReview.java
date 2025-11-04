package org.example;


public class OneReview
{
    private String feedbackID;
    private String feedbackText;
    private String feedbackTextEmotions;

    public OneReview(String feedbackID, String feedbackText)
    {
        this.feedbackID = feedbackID;
        this.feedbackText = feedbackText;
        this.feedbackTextEmotions = null;
    }

    public String getTextEmotions()
    {
        return feedbackTextEmotions;
    }

    public void setTextEmotions(String textEmotions)
    {
        this.feedbackTextEmotions = textEmotions;
    }

}

