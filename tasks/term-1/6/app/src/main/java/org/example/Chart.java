package knn.classifier;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class Chart {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 50;
    private static final int POINT_SIZE = 8;

    private static final Map<String, Color> COLOR_MAP = new HashMap<>();
    static {
        COLOR_MAP.put("A", Color.RED);
        COLOR_MAP.put("B", Color.BLUE);
        COLOR_MAP.put("C", Color.GREEN);
        COLOR_MAP.put("D", Color.ORANGE);
        COLOR_MAP.put("E", Color.MAGENTA);
        COLOR_MAP.put("UNKNOWN", Color.GRAY);
    }

    public static void generateChart(List<Point> points, Point unknownPoint, String outputPath) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE); //фон
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        double minX = Double.MAX_VALUE; //находим границы
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Point point : points) {
            minX = Math.min(minX, point.getX());
            maxX = Math.max(maxX, point.getX());
            minY = Math.min(minY, point.getY());
            maxY = Math.max(maxY, point.getY());
        }

        if (unknownPoint != null) {
            minX = Math.min(minX, unknownPoint.getX());
            maxX = Math.max(maxX, unknownPoint.getX());
            minY = Math.min(minY, unknownPoint.getY());
            maxY = Math.max(maxY, unknownPoint.getY());
        }

        double xRange = maxX - minX; //+ отступы
        double yRange = maxY - minY;
        final double finalMinX = minX - xRange * 0.1;
        final double finalMaxX = maxX + xRange * 0.1;
        final double finalMinY = minY - yRange * 0.1;
        final double finalMaxY = maxY + yRange * 0.1;

        g2d.setColor(Color.BLACK); //оси
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, WIDTH - MARGIN, HEIGHT - MARGIN); // X-axis
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, MARGIN, MARGIN); // Y-axis

        for (Point point : points) { //точки
            int x = scaleX(point.getX(), finalMinX, finalMaxX);
            int y = scaleY(point.getY(), finalMinY, finalMaxY);
            Color color = COLOR_MAP.getOrDefault(point.getLabel(), Color.BLACK);
            g2d.setColor(color);
            g2d.fillOval(x - POINT_SIZE/2, y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawString(point.getLabel(), x + POINT_SIZE, y - POINT_SIZE);
        }

        if (unknownPoint != null) { //рисуем неклассифицированные точки
            int x = scaleX(unknownPoint.getX(), finalMinX, finalMaxX);
            int y = scaleY(unknownPoint.getY(), finalMinY, finalMaxY);
            g2d.setColor(Color.GRAY);
            g2d.fillRect(x - POINT_SIZE/2, y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawString("?", x + POINT_SIZE, y - POINT_SIZE);
        }


        drawLegend(g2d, points); //легенда
        g2d.dispose();
        ImageIO.write(image, "png", new File(outputPath));
    }

    private static int scaleX(double x, double minX, double maxX) {
        return (int) (MARGIN + (x - minX) * (WIDTH - 2 * MARGIN) / (maxX - minX));
    }

    private static int scaleY(double y, double minY, double maxY) {
        return (int) (HEIGHT - MARGIN - (y - minY) * (HEIGHT - 2 * MARGIN) / (maxY - minY));
    }

    private static void drawLegend(Graphics2D g2d, List<Point> points) {
        Set<String> labels = new HashSet<>();
        for (Point point : points) {
            labels.add(point.getLabel());
        }

        int legendX = WIDTH - 150;
        int legendY = 50;
        int lineHeight = 20;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(legendX - 10, legendY - 10, 140, labels.size() * lineHeight + 10);

        g2d.setColor(Color.BLACK);
        g2d.drawString("Legend:", legendX, legendY);

        int i = 0;
        for (String label : labels) {
            Color color = COLOR_MAP.getOrDefault(label, Color.BLACK);
            g2d.setColor(color);
            g2d.fillOval(legendX, legendY + 15 + i * lineHeight, 10, 10);

            g2d.setColor(Color.BLACK);
            g2d.drawString("Class " + label, legendX + 15, legendY + 25 + i * lineHeight);
            i++;
        }
    }
}
