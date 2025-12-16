package knn;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

import javax.swing.JFrame;

public class Plotter {

    private static final Color[] COLORS = {
        Color.RED, Color.BLUE, Color.GREEN,
        Color.ORANGE, Color.MAGENTA, Color.CYAN
    };

    public static void plot(List<Point> points) {
        BufferedImage image = createImage(points);
        showPlot(image);
    }

    private static BufferedImage createImage(List<Point> points) {
        int width = 1280;
        int height = 720;
        int boundIndent = 50;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = image.createGraphics();

        gr.setColor(Color.WHITE);
        gr.fillRect(0, 0, width, height);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Map<String, Color> classColors = new HashMap<>();
        int i = 0;

        for (Point point : points) {
            if (!classColors.containsKey(point.getLabel())) {
                classColors.put(point.getLabel(), COLORS[i++ % COLORS.length]);
            }
        }

        double minX = points.stream().mapToDouble(Point::getX).min().orElse(0);
        double maxX = points.stream().mapToDouble(Point::getX).max().orElse(1);
        double minY = points.stream().mapToDouble(Point::getY).min().orElse(0);
        double maxY = points.stream().mapToDouble(Point::getY).max().orElse(1);

        double scaleX = (width - 2 * boundIndent) / (maxX - minX);
        double scaleY = (height - 2 * boundIndent) / (maxY - minY);

        for (Point p : points) {

            int px = (int) (boundIndent + (p.getX() - minX) * scaleX);
            int py = (int) (height - boundIndent - (p.getY() - minY) * scaleY);

            gr.setColor(classColors.get(p.getLabel()));

            Shape circle = new Ellipse2D.Double(px - 5, py - 5, 10, 10);
            gr.fill(circle);
        }

        gr.dispose();

        return image;
    }

    private static void showPlot(BufferedImage image) {
        JFrame frame = new JFrame("KNN");
        frame.setSize(image.getWidth(), image.getHeight());
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame = new JFrame("KNN") {
            @Override
            public void paint(Graphics gr) {
                super.paint(gr);
                gr.drawImage(image, 0, 30, null);
            }
        };

        frame.setSize(image.getWidth(), image.getHeight());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }




}