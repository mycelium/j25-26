package org.example.sentiment;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.example.dataset.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Класс для анализа тональности отзывов с помощью Stanford CoreNLP.
 */
public class SentimentAnalyzer
{
    private final StanfordCoreNLP pipeline;

    /**
     * Создаёт и настраивает pipeline для sentiment analysis.
     */
    public SentimentAnalyzer()
    {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
        props.setProperty("tokenize.language", "en");
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Выполняет анализ тональности для списка отзывов.
     *
     * @param reviews список отзывов
     * @return список предсказанных меток
     */
    public List<String> predictAll(List<Review> reviews)
    {
        List<String> predictions = new ArrayList<>();

        for (Review review : reviews)
            predictions.add(predict(review.text()));

        return predictions;
    }

    /**
     * Определяет итоговую тональность одного отзыва.
     *
     * @param text текст отзыва
     * @return метка тональности: negative, neutral или positive
     */
    public String predict(String text)
    {
        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        int sum = 0;

        for (var sentence : document.sentences())
            sum += mapToScore(sentence.sentiment());

        double avg = (double) sum / document.sentences().size();
        return mapToLabel(avg);
    }

    /**
     * Переводит оценку Stanford CoreNLP в числовую шкалу.
     *
     * @param sentiment тональность предложения
     * @return число от 0 до 4
     */
    private int mapToScore(String sentiment){
        switch (sentiment.toLowerCase())
        {
            case "very negative":
                return 0;
            case "negative":
                return 1;
            case "neutral":
                return 2;
            case "positive":
                return 3;
            default:
                return 4;
        }
    }

    /**
     * Переводит среднюю числовую оценку в итоговую метку тональности.
     *
     * @param score средняя оценка
     * @return negative, neutral или positive
     */
    private String mapToLabel(double score)
    {
        if (score < 1.8)
            return "negative";
        if (score > 2.2)
            return "positive";
        return "neutral";
    }
}