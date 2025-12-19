package org.knn;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class Graph {

    private static final int SIZE = 800;
    private static final int MARGIN = 60;
    private static final int POINT_SIZE = 10;

    public static void save(String file, List<Point> train, Point testPoint, String predicted) {
        try {
            BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();

            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, SIZE, SIZE);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

            for (Point p : train) {
                minX = Math.min(minX, p.x);
                maxX = Math.max(maxX, p.x);
                minY = Math.min(minY, p.y);
                maxY = Math.max(maxY, p.y);
            }

            minX -= 1; maxX += 1;
            minY -= 1; maxY += 1;

            double scaleX = (SIZE - 2 * MARGIN) / (maxX - minX);
            double scaleY = (SIZE - 2 * MARGIN) / (maxY - minY);

            graphics.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= 10; i++) {
                int x = MARGIN + i * (SIZE - 2 * MARGIN) / 10;
                int y = MARGIN + i * (SIZE - 2 * MARGIN) / 10;

                graphics.drawLine(x, MARGIN, x, SIZE - MARGIN);
                graphics.drawLine(MARGIN, y, SIZE - MARGIN, y);
            }

            graphics.setColor(Color.BLACK);
            graphics.drawLine(MARGIN, SIZE - MARGIN, SIZE - MARGIN, SIZE - MARGIN); // X
            graphics.drawLine(MARGIN, MARGIN, MARGIN, SIZE - MARGIN);               // Y

            graphics.drawString("X", SIZE - MARGIN + 10, SIZE - MARGIN + 5);
            graphics.drawString("Y", MARGIN - 20, MARGIN - 10);

            for (Point p : train) {
                graphics.setColor(colorForLabel(p.label));

                int x = (int) (MARGIN + (p.x - minX) * scaleX);
                int y = (int) (SIZE - MARGIN - (p.y - minY) * scaleY);

                graphics.fillOval(x - POINT_SIZE/2, y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
            }

            int tx = (int)(MARGIN + (testPoint.x - minX) * scaleX);
            int ty = (int)(SIZE - MARGIN - (testPoint.y - minY) * scaleY);

            graphics.setColor(Color.BLACK);
            graphics.fillOval(tx - 12, ty - 12, 24, 24);
            graphics.setColor(colorForLabel(predicted));
            graphics.fillOval(tx - 8, ty - 8, 16, 16);

            graphics.setColor(Color.BLACK);
            drawLegend(graphics);

            ImageIO.write(image, "png", new File(file));
            System.out.println("Graph saved to " + file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void drawLegend(Graphics2D g) {
        int x = SIZE - 100;
        int y = 40;
        int box = 14;
        int step = 22;

        g.setFont(new Font("Arial", Font.PLAIN, 20));

        g.setColor(Color.BLACK);

        String[] labels = {"A", "B", "C"};
        for (int i = 0; i < labels.length; i++) {
            String lbl = labels[i];
            g.setColor(colorForLabel(lbl));
            g.fillRect(x, y + 10 + i * step, box, box);

            g.setColor(Color.BLACK);
            g.drawRect(x, y + 10 + i * step, box, box);
            g.drawString(lbl, x + box + 10, y + 10 + i * step + 12);
        }
    }

    private static Color colorForLabel(String label) {
        return switch (label) {
            case "A" -> Color.MAGENTA;
            case "B" -> Color.PINK;
            case "C" -> Color.CYAN;
            default -> Color.GRAY;
        };
    }
}
