package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Класс реализует классификатор K-Nearest Neighbors (KNN) для двумерных точек.
 */
public class PointClassifier {
    /** Количество ближайших соседей, используемых в алгоритме KNN */
    private static final int K = 3;

    /** Набор точек обучающей выборки */
    private final List<PointData> points = new ArrayList<>();

    /**
     * Добавляет новую точку в набор данных.
     *
     * @param x координата X
     * @param y координата Y
     * @param classLabel номер класса точки
     */
    public void addPoint(double x, double y, int classLabel) {
        points.add(new PointData(x, y, classLabel));
    }


    /**
     * Определяет класс новой точки с помощью алгоритма KNN.
     * 
     * @param x координата X новой точки
     * @param y координата Y новой точки
     * @return предсказанный класс
     */
    public int classify(double x, double y) {

        List<Neighbor> neighbors = new ArrayList<>();
        for (PointData p : points) {

            double dx = x - p.x();
            double dy = y - p.y();
            double dist = Math.sqrt(dx * dx + dy * dy);

            neighbors.add(new Neighbor(p.classLabel(), dist));
        }
        neighbors.sort(Comparator.comparingDouble(Neighbor::distance));

        Map<Integer, Integer> votes = new HashMap<>();

        for (int i = 0; i < K; i++) {
            int label = neighbors.get(i).classLabel();
            votes.put(label, votes.getOrDefault(label, 0) + 1);
        }

        int bestClass = -1;
        int bestVotes = -1;

        for (var e : votes.entrySet())
            if (e.getValue() > bestVotes) {
                bestVotes = e.getValue();
                bestClass = e.getKey();
            }

        return bestClass;
    }


    /**
     * Рисует изображение 
     * 
     * @param newX координата X новой точки
     * @param newY координата Y новой точки
     * @param predictedClass предсказанный класс
     * @param fileName имя выходного файла
     */
    public void drawPoints(double newX, double newY, int predictedClass, String fileName) throws Exception {

        int width = 800;
        int height = 600;
        int margin = 60;

        // определяем границы координат
        double minX = points.get(0).x();
        double maxX = points.get(0).x();
        double minY = points.get(0).y();
        double maxY = points.get(0).y();

        for (PointData p : points) {
            minX = Math.min(minX, p.x());
            maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = Math.max(maxY, p.y());
        }

        // создаём изображение
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // фон
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // рисуем точки обучающей выборки
        for (PointData p : points) {

            int px = margin + (int)((p.x() - minX) / (maxX - minX) * (width - 2 * margin));
            int py = height - margin - (int)((p.y() - minY) / (maxY - minY) * (height - 2 * margin));

            g.setColor(color(p.classLabel()));
            g.fillOval(px - 6, py - 6, 12, 12);
        }

        // координаты новой точки
        int px = margin + (int)((newX - minX) / (maxX - minX) * (width - 2 * margin));
        int py = height - margin - (int)((newY - minY) / (maxY - minY) * (height - 2 * margin));

        // рисуем новую точку
        g.setColor(color(predictedClass));
        g.fillOval(px - 10, py - 10, 20, 20);

        g.setColor(Color.BLACK);
        g.drawString("Predicted class = " + predictedClass, 20, 20);

        g.dispose();

        // сохраняем изображение
        ImageIO.write(image, "png", new File(fileName));
    }


    /**
     * Возвращает цвет для класса точки.
     */
    private Color color(int c) {
        return switch (c) {
            case 1 -> Color.RED;
            case 2 -> Color.BLUE;
            default -> Color.GREEN;
        };
    }


    /**
     * Вспомогательная структура.
     * Используется для хранения расстояния до соседней точки.
     */
    private record Neighbor(int classLabel, double distance) {}
}