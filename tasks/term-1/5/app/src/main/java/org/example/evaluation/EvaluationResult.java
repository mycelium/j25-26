package org.example.evaluation;

/**
 * Хранит результаты оценки качества анализа тональности.
 */
public class EvaluationResult
{
    int total;

    int correctPositive;
    int correctNegative;
    int correctNeutral;

    int predictedPositive;
    int predictedNegative;
    int predictedNeutral;

    public int getCorrectTotal()
    {
        return correctPositive + correctNegative + correctNeutral;
    }

    public double getAccuracy()
    {
        if (total == 0)
            return 0.0;

        return 100.0 * getCorrectTotal() / total;
    }

    public void print()
    {
        System.out.println("\nEvaluation results:");
        System.out.println("Correct positive: " + correctPositive);
        System.out.println("Correct negative: " + correctNegative);
        System.out.println("Correct neutral: " + correctNeutral);
        System.out.println("Correct total: " + getCorrectTotal());
        System.out.printf("Accuracy: %.2f%%%n", getAccuracy());

        System.out.println("\nPredicted labels:");
        System.out.println("Predicted positive: " + predictedPositive);
        System.out.println("Predicted negative: " + predictedNegative);
        System.out.println("Predicted neutral: " + predictedNeutral);
    }
}