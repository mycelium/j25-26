// src/main/java/org/example/Plotter.java
package org.example;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Plotter {

    public static void plot(List<App.Point> train, App.Point query, String pred, String filename) {
        int W = 800, H = 600, P = 50, R = 8;
        Color[] COLORS = { Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE };

        try {
            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

            for (App.Point p : train) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minY = Math.min(minY, p.getY());
                maxY = Math.max(maxY, p.getY());
            }
            minX = Math.min(minX, query.getX());
            maxX = Math.max(maxX, query.getX());
            minY = Math.min(minY, query.getY());
            maxY = Math.max(maxY, query.getY());

            double padX = (maxX - minX) * 0.1;
            double padY = (maxY - minY) * 0.1;
            minX -= Math.max(padX, 0.5);
            maxX += Math.max(padX, 0.5);
            minY -= Math.max(padY, 0.5);
            maxY += Math.max(padY, 0.5);

            if (maxX == minX)
                maxX = minX + 1;
            if (maxY == minY)
                maxY = minY + 1;

            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, W, H);

            Set<String> labels = new LinkedHashSet<>();
            for (App.Point p : train)
                labels.add(p.getLabel());
            Map<String, Color> colorMap = new HashMap<>();
            int i = 0;
            for (String lbl : labels) {
                colorMap.put(lbl, COLORS[i++ % COLORS.length]);
            }

            int plotWidth = W - 2 * P;
            int plotHeight = H - 2 * P;

            for (App.Point p : train) {
                int px = P + (int) ((p.getX() - minX) / (maxX - minX) * plotWidth);
                int py = H - P - (int) ((p.getY() - minY) / (maxY - minY) * plotHeight);
                g.setColor(colorMap.get(p.getLabel()));
                g.fillOval(px - R, py - R, 2 * R, 2 * R);
            }

            int qx = P + (int) ((query.getX() - minX) / (maxX - minX) * plotWidth);
            int qy = H - P - (int) ((query.getY() - minY) / (maxY - minY) * plotHeight);
            g.setColor(Color.BLACK);
            g.fillOval(qx - R - 2, qy - R - 2, 2 * R + 4, 2 * R + 4);
            g.setColor(colorMap.get(pred));
            g.fillOval(qx - R, qy - R, 2 * R, 2 * R);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            int y = P + 20;
            g.drawString("Classes:", P + 10, y);
            y += 25;
            for (String lbl : labels) {
                g.setColor(colorMap.get(lbl));
                g.fillOval(P + 10, y - R, 2 * R, 2 * R);
                g.setColor(Color.BLACK);
                g.drawString(lbl, P + 10 + 2 * R + 8, y);
                y += 25;
            }
            g.drawString("Pount: " + String.format("(%.1f, %.1f)", query.getX(), query.getY()), P + 10, y);
            y += 20;
            g.drawString("Assumption: " + pred, P + 10, y);

            ImageIO.write(img, "PNG", new File(filename));
            System.out.println("Saved to " + filename);
            g.dispose();

        } catch (IOException e) {
            System.err.println("Saving error: " + e.getMessage());
        }
    }
}