package org.example;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SimplePlotter {

    public static void drawPoints(List<Point> points, String fileName,
                                  int width, int height,
                                  double minX, double maxX,
                                  double minY, double maxY) {

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, height / 2, width, height / 2);
        g.drawLine(width / 2, 0, width / 2, height);

        for (Point p : points) {
            Color color = getColorByClass(p.clazz);
            g.setColor(color);

            int px = mapToScreen(p.x, minX, maxX, width);
            int py = height - mapToScreen(p.y, minY, maxY, height);

            int size = 8;
            g.fillOval(px - size / 2, py - size / 2, size, size);
        }

        g.dispose();

        try {
            ImageIO.write(img, "png", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int mapToScreen(double value, double min, double max, int size) {
        if (max - min == 0) {
            return size / 2;
        }
        double t = (value - min) / (max - min);
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        return (int) (t * (size - 1));
    }

    private static Color getColorByClass(int clazz) {
        switch (clazz) {
            case 0:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.MAGENTA;
            case 4:
                return Color.ORANGE;
            default:
                return Color.BLACK;
        }
    }
}