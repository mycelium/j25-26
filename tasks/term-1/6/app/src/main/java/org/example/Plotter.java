package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Plotter {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDING = 50;

    public void plot(List<Point> trainingData, Point classifiedPoint, String filename) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (Point p : trainingData) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }
        
        if (classifiedPoint.getX() < minX) minX = classifiedPoint.getX();
        if (classifiedPoint.getX() > maxX) maxX = classifiedPoint.getX();
        if (classifiedPoint.getY() < minY) minY = classifiedPoint.getY();
        if (classifiedPoint.getY() > maxY) maxY = classifiedPoint.getY();

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;
        if (rangeX == 0) rangeX = 1; 
        if (rangeY == 0) rangeY = 1;
        
        minX -= rangeX * 0.1;
        maxX += rangeX * 0.1;
        minY -= rangeY * 0.1;
        maxY += rangeY * 0.1;

        Map<String, Color> classColors = new HashMap<>();
        Random random = new Random(42);

        g2d.setColor(Color.BLACK);
        g2d.drawLine(PADDING, HEIGHT - PADDING, WIDTH - PADDING, HEIGHT - PADDING);
        g2d.drawLine(PADDING, PADDING, PADDING, HEIGHT - PADDING);

        CoordinateMapper mapper = new CoordinateMapper(minX, maxX, minY, maxY, PADDING, WIDTH - PADDING, HEIGHT - PADDING, PADDING);

        for (Point p : trainingData) {
            String label = p.getLabel();
            classColors.computeIfAbsent(label, k -> new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            
            g2d.setColor(classColors.get(label));
            int x = mapper.mapX(p.getX());
            int y = mapper.mapY(p.getY());
            g2d.fill(new Ellipse2D.Double(x - 4, y - 4, 8, 8));
        }

        g2d.setColor(classColors.get(classifiedPoint.getLabel()));
        int x = mapper.mapX(classifiedPoint.getX());
        int y = mapper.mapY(classifiedPoint.getY());
        
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(x - 8, y - 8, x + 8, y + 8);
        g2d.drawLine(x + 8, y - 8, x - 8, y + 8);
        
        int legendY = PADDING;
        for(Map.Entry<String, Color> entry : classColors.entrySet()) {
             g2d.setColor(entry.getValue());
             g2d.fillRect(WIDTH - PADDING - 100, legendY, 15, 15);
             g2d.setColor(Color.BLACK);
             g2d.drawString(entry.getKey(), WIDTH - PADDING - 80, legendY + 12);
             legendY += 20;
        }

        g2d.dispose();

        try {
            ImageIO.write(image, "PNG", new File(filename));
            System.out.println("Plot saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class CoordinateMapper {
        double minX, maxX, minY, maxY;
        int screenMinX, screenMaxX, screenMinY, screenMaxY;

        public CoordinateMapper(double minX, double maxX, double minY, double maxY, int screenMinX, int screenMaxX, int screenMinY, int screenMaxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.screenMinX = screenMinX;
            this.screenMaxX = screenMaxX;
            this.screenMinY = screenMinY;
            this.screenMaxY = screenMaxY;
        }

        public int mapX(double x) {
            return (int) (screenMinX + (x - minX) / (maxX - minX) * (screenMaxX - screenMinX));
        }

        public int mapY(double y) {
             return (int) (screenMinY - (y - minY) / (maxY - minY) * (screenMinY - screenMaxY));
        }
    }
}

