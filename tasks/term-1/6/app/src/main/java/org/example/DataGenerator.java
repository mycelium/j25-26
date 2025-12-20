package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Генерирует пример обучающих данных с несколькими классами
 */
public class DataGenerator {
    private final Random random = new Random(42); // Фиксированное зерно для воспроизводимости

    /**
     * Генерирует пример данных с 3 классами, расположенными в виде кластеров
     */
    public List<Point> generateSampleData(int pointsPerClass) {
        List<Point> data = new ArrayList<>();

        // Класс A: центр в точке (2, 2)
        data.addAll(generateCluster("A", 2.0, 2.0, pointsPerClass, 0.8));

        // Класс B: центр в точке (8, 3)
        data.addAll(generateCluster("B", 8.0, 3.0, pointsPerClass, 0.8));

        // Класс C: центр в точке (5, 8)
        data.addAll(generateCluster("C", 5.0, 8.0, pointsPerClass, 0.8));

        return data;
    }

    /**
     * Генерирует кластер точек вокруг центра
     */
    private List<Point> generateCluster(String label, double centerX, double centerY,
                                      int count, double spread) {
        List<Point> cluster = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Генерация случайной точки в пределах заданного радиуса от центра
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = random.nextDouble() * spread;

            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            cluster.add(new Point(x, y, label));
        }

        return cluster;
    }

    /**
     * Генерирует тестовые точки для классификации
     */
    public List<Point> generateTestPoints(int count) {
        List<Point> testPoints = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double x = random.nextDouble() * 12; // Диапазон 0–12
            double y = random.nextDouble() * 12; // Диапазон 0–12
            testPoints.add(new Point(x, y));
        }

        return testPoints;
    }

    /**
     * Возвращает цвет класса для визуализации
     */
    public static java.awt.Color getClassColor(String label) {
        switch (label) {
            case "A": return java.awt.Color.RED;
            case "B": return java.awt.Color.BLUE;
            case "C": return java.awt.Color.GREEN;
            default: return java.awt.Color.BLACK;
        }
    }
}
