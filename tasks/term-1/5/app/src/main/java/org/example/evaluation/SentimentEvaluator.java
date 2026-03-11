package org.example.evaluation;

import org.example.dataset.Review;

import java.util.List;

/**
 * Класс для сравнения ожидаемых и предсказанных меток тональности.
 */
public class SentimentEvaluator
{
    /**
     * Сравнивает реальные и предсказанные метки и формирует результат оценки.
     *
     * @param reviews список отзывов
     * @param predictions список предсказанных меток
     * @return результат оценки
     */
    public static EvaluationResult evaluate(List<Review> reviews, List<String> predictions)
    {
        EvaluationResult result = new EvaluationResult();

        for (int i = 0; i < reviews.size(); i++)
        {
            String expected = normalize(reviews.get(i).sentiment());
            String predicted = normalize(predictions.get(i));

            result.total++;

            switch (predicted)
            {
                case "positive":
                    result.predictedPositive++;
                    break;
                case "negative":
                    result.predictedNegative++;
                    break;
                case "neutral":
                    result.predictedNeutral++;
                    break;
            }

            if (expected.equals(predicted))
                switch (expected)
                {
                    case "positive":
                        result.correctPositive++;
                        break;
                    case "negative":
                        result.correctNegative++;
                        break;
                    case "neutral":
                        result.correctNeutral++;
                        break;
                }
        }

        return result;
    }

    /**
     * Приводит метку к единому виду.
     *
     * @param value исходная метка
     * @return нормализованная метка
     */
    private static String normalize(String value)
    {
        return value.trim().toLowerCase();
    }
}