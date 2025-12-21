package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;

public class Chart {
    private int width = 800;
    private int height = 600;
    private int margin = 50;

    public Chart() {
    }

    public void drawPoints(List<Point> points, Point testPoint, int testPointClass, String filename) throws IOException {

        BufferedImage image = null;
        Graphics2D g = null;
        try {

            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            g = image.createGraphics();

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            if (points.isEmpty()) {
                ImageIO.write(image, "PNG", new File(filename));
                return;
            }

            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            for (Point p : points) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minY = Math.min(minY, p.getY());
                maxY = Math.max(maxY, p.getY());
            }

            if (testPoint != null) {
                minX = Math.min(minX, testPoint.getX());
                maxX = Math.max(maxX, testPoint.getX());
                minY = Math.min(minY, testPoint.getY());
                maxY = Math.max(maxY, testPoint.getY());
            }

            double rangeX = maxX - minX;
            double rangeY = maxY - minY;
            minX -= rangeX * 0.1;
            maxX += rangeX * 0.1;
            minY -= rangeY * 0.1;
            maxY += rangeY * 0.1;

            g.setColor(Color.BLACK);
            g.drawLine(margin, margin, margin, height - margin);
            g.drawLine(margin, height - margin, width - margin, height - margin);

            Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};

            for (Point p : points) {
                int x = toX(p.getX(), minX, maxX);
                int y = toY(p.getY(), minY, maxY);

                int classId = p.getClassLabel();
                Color color = colors[Math.abs(classId) % colors.length];

                g.setColor(color);
                g.fillOval(x - 4, y - 4, 8, 8);
                g.setColor(Color.BLACK);
                g.drawOval(x - 4, y - 4, 8, 8);
            }

            if (testPoint != null) {
                int x = toX(testPoint.getX(), minX, maxX);
                int y = toY(testPoint.getY(), minY, maxY);

                Color color = colors[Math.abs(testPointClass) % colors.length];

                g.setColor(color);
                g.fillRect(x - 6, y - 6, 12, 12);
                g.setColor(Color.BLACK);
                g.drawRect(x - 6, y - 6, 12, 12);

                g.drawString("Test:" + testPointClass, x + 15, y - 15);
            }

            ImageIO.write(image, "PNG", new File(filename));
        } finally {
            if(g != null){
                g.dispose();
            }
        }
    }

    private int toX(double x, double minX, double maxX) {
        return margin + (int)((x - minX) * (width - 2 * margin) / (maxX - minX));
    }

    private int toY(double y, double minY, double maxY) {
        return height - margin - (int)((y - minY) * (height - 2 * margin) / (maxY - minY));
    }

}