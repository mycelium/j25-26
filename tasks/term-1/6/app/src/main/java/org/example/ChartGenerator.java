package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ChartGenerator {
    private final List<Point> points;
    private final Map<String, Color> classColors;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 50;
    private static final int POINT_RADIUS = 4;

    public ChartGenerator(List<Point> points) {
        this.points = points;
        this.classColors = generateClassColors();
    }

    private Map<String, Color> generateClassColors() {
        Set<String> uniqueLabels = points.stream()
                .map(Point::getLabel)
                .collect(Collectors.toSet());

        Map<String, Color> colors = new HashMap<>();
        List<Color> palette = Arrays.asList(
                Color.RED, Color.BLUE, Color.GREEN,
                Color.MAGENTA, Color.ORANGE, Color.CYAN
        );

        int index = 0;
        for (String label : uniqueLabels) {
            colors.put(label, palette.get(index % palette.size()));
            index++;
        }
        return colors;
    }

    public void saveChart(String filename) throws IOException {
        double minX = points.stream().mapToDouble(Point::getX).min().orElse(0);
        double maxX = points.stream().mapToDouble(Point::getX).max().orElse(1);
        double minY = points.stream().mapToDouble(Point::getY).min().orElse(0);
        double maxY = points.stream().mapToDouble(Point::getY).max().orElse(1);

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g);
        drawPoints(g, minX, maxX, minY, maxY);
        drawLegend(g);

        ImageIO.write(image, "png", new File(filename));
        g.dispose();
    }

    private void drawBackground(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(MARGIN, MARGIN, WIDTH - 2 * MARGIN, HEIGHT - 2 * MARGIN);
    }

    private void drawPoints(Graphics2D g, double minX, double maxX, double minY, double maxY) {
        double xScale = (WIDTH - 2 * MARGIN) / (maxX - minX);
        double yScale = (HEIGHT - 2 * MARGIN) / (maxY - minY);
        double scale = Math.min(xScale, yScale);

        for (Point p : points) {
            int x = (int) ((p.getX() - minX) * scale + MARGIN);
            int y = HEIGHT - (int) ((p.getY() - minY) * scale + MARGIN);

            g.setColor(classColors.get(p.getLabel()));
            g.fillOval(x - POINT_RADIUS, y - POINT_RADIUS,
                    POINT_RADIUS * 2, POINT_RADIUS * 2);
        }
    }

    private void drawLegend(Graphics2D g) {
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        int legendX = WIDTH - 150;
        int legendY = MARGIN + 30;

        g.setColor(Color.BLACK);
        g.drawString("Legend", legendX, legendY - 10);

        int yOffset = 0;
        for (Map.Entry<String, Color> entry : classColors.entrySet()) {
            g.setColor(entry.getValue());
            g.fillOval(legendX, legendY + yOffset, 15, 15);

            g.setColor(Color.BLACK);
            g.drawString(entry.getKey(), legendX + 25, legendY + yOffset + 12);

            yOffset += 25;
        }
    }
}
