package org.example;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Генерирует PNG-графики для визуализации точек
 */
public class PlotGenerator {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 50;
    private static final int POINT_SIZE = 8;

    /**
     * Создаёт график обучающих данных
     */
    public void createTrainingDataPlot(List<Point> trainingData, String filename) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Установка фона
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Отрисовка осей и сетки
        drawGrid(g2d);

        // Отрисовка обучающих точек
        for (Point point : trainingData) {
            drawPoint(g2d, point, DataGenerator.getClassColor(point.getLabel()), POINT_SIZE);
        }

        // Отрисовка легенды
        drawLegend(g2d, trainingData);

        g2d.dispose();
        saveImage(image, filename);
    }

    /**
     * Создаёт график, отображающий обучающие данные и классифицированные тестовые точки
     */
    public void createClassificationPlot(List<Point> trainingData, List<Point> testPoints,
                                       List<String> predictions, String filename) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Установка фона
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Отрисовка осей и сетки
        drawGrid(g2d);

        // Отрисовка обучающих точек (меньшего размера)
        for (Point point : trainingData) {
            drawPoint(g2d, point, DataGenerator.getClassColor(point.getLabel()), POINT_SIZE / 2);
        }

        // Отрисовка тестовых точек с результатами классификации
        for (int i = 0; i < testPoints.size(); i++) {
            Point testPoint = testPoints.get(i);
            String prediction = predictions.get(i);
            Color color = DataGenerator.getClassColor(prediction);

            // Отрисовка увеличенной точки с чёрной рамкой для тестовых данных
            drawPointWithBorder(g2d, testPoint, color, POINT_SIZE + 4);
        }

        // Отрисовка легенды
        drawLegend(g2d, trainingData);

        g2d.dispose();
        saveImage(image, filename);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.LIGHT_GRAY);

        // Отрисовка линий сетки
        for (int i = 0; i <= 12; i++) {
            int x = MARGIN + (int)((WIDTH - 2 * MARGIN) * i / 12.0);
            int y = MARGIN + (int)((HEIGHT - 2 * MARGIN) * i / 12.0);

            // Вертикальные линии
            g2d.drawLine(x, MARGIN, x, HEIGHT - MARGIN);
            // Горизонтальные линии
            g2d.drawLine(MARGIN, y, WIDTH - MARGIN, y);
        }

        // Отрисовка осей
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        // Ось X
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, WIDTH - MARGIN, HEIGHT - MARGIN);
        // Ось Y
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, MARGIN, MARGIN);

        // Подписи осей
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i <= 12; i += 2) {
            int x = MARGIN + (int)((WIDTH - 2 * MARGIN) * i / 12.0);
            int y = MARGIN + (int)((HEIGHT - 2 * MARGIN) * i / 12.0);

            // Подписи по оси X
            g2d.drawString(String.valueOf(i), x - 5, HEIGHT - MARGIN + 20);
            // Подписи по оси Y
            g2d.drawString(String.valueOf(12 - i), MARGIN - 25, y + 5);
        }
    }

    private void drawPoint(Graphics2D g2d, Point point, Color color, int size) {
        int x = (int) (MARGIN + (WIDTH - 2 * MARGIN) * point.getX() / 12.0);
        int y = (int) (HEIGHT - MARGIN - (HEIGHT - 2 * MARGIN) * point.getY() / 12.0);

        g2d.setColor(color);
        g2d.fillOval(x - size/2, y - size/2, size, size);
    }

    private void drawPointWithBorder(Graphics2D g2d, Point point, Color color, int size) {
        int x = (int) (MARGIN + (WIDTH - 2 * MARGIN) * point.getX() / 12.0);
        int y = (int) (HEIGHT - MARGIN - (HEIGHT - 2 * MARGIN) * point.getY() / 12.0);

        // Отрисовка рамки
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x - size/2, y - size/2, size, size);

        // Отрисовка внутреннего цвета
        g2d.setColor(color);
        g2d.fillOval(x - (size-4)/2, y - (size-4)/2, size-4, size-4);
    }

    private void drawLegend(Graphics2D g2d, List<Point> trainingData) {
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        int legendX = WIDTH - 150;
        int legendY = MARGIN + 20;

        g2d.setColor(Color.BLACK);
        g2d.drawString("Classes:", legendX, legendY);

        // Получение уникальных классов
        java.util.Set<String> classes = new java.util.HashSet<>();
        for (Point point : trainingData) {
            classes.add(point.getLabel());
        }

        int yOffset = legendY + 20;
        for (String label : classes) {
            g2d.setColor(DataGenerator.getClassColor(label));
            g2d.fillOval(legendX, yOffset - 5, 10, 10);

            g2d.setColor(Color.BLACK);
            g2d.drawString("Class " + label, legendX + 15, yOffset + 5);

            yOffset += 20;
        }

        // Добавление пояснения для тестовых точек
        yOffset += 10;
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.drawString("Large points = test data", legendX - 20, yOffset);
    }

    private void saveImage(BufferedImage image, String filename) throws IOException {
        File outputFile = new File(filename);
        ImageIO.write(image, "png", outputFile);
        System.out.println("Plot saved to: " + outputFile.getAbsolutePath());
    }
}
