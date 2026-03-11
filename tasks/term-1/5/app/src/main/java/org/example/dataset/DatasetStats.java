package org.example.dataset;

public class DatasetStats
{
    private int total;
    private int positive;
    private int negative;
    private int neutral;

    public void add(String sentiment)
    {
        total++;

        if (sentiment == null) return;

        switch (sentiment.toLowerCase())
        {
            case "positive":
                positive++;
                break;

            case "negative":
                negative++;
                break;

            case "neutral":
                neutral++;
                break;
        }
    }

    public int getTotal()
    {
        return total;
    }

    public int getPositive()
    {
        return positive;
    }

    public int getNegative()
    {
        return negative;
    }

    public int getNeutral()
    {
        return neutral;
    }

    public void print()
    {
        System.out.println("Dataset statistics:");
        System.out.println("Total reviews: " + total);
        System.out.println("Positive: " + positive);
        System.out.println("Negative: " + negative);
        System.out.println("Neutral: " + neutral);
    }
}