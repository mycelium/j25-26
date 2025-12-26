package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class KNNCanvas {

    public static void show(DataSet trainSet, List<Point> tests, String title) {

        List<Point> train = trainSet.getPoints();
        BufferedImage img = buildImage(train, tests, title);

        SwingUtilities.invokeLater(() -> openWindow(img, "KNN demo"));
    }

    private static BufferedImage buildImage(List<Point> train, List<Point> tests, String title) {

        int width = 1080;
        int height = 720;
        int padding = 60;
        int legendWidth = 120;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        enableAntialias(g);

        g.setColor(new Color(245, 248, 252));
        g.fillRect(0, 0, width, height);

        Bounds b = Bounds.fromData(mergeLists(train, tests));
        double scaleX = (width - 2.0 * padding - legendWidth) / (b.maxX() - b.minX());
        double scaleY = (height - 2.0 * padding) / (b.maxY() - b.minY());

        drawAxes(g, width, height, padding, legendWidth, b, scaleX, scaleY);
        Map<String, Color> palette = makePalette(train, tests);

        drawTrainPoints(g, train, b, scaleX, scaleY, padding, height, palette);
        if (tests != null && !tests.isEmpty()) {
            drawTestPoints(g, tests, b, scaleX, scaleY, padding, height, palette);
        }

        drawTitleAndLegend(g, width, padding, legendWidth, title, palette, tests != null && !tests.isEmpty());
        g.dispose();
        return img;
    }

    private static void openWindow(BufferedImage img, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel label = new JLabel(new ImageIcon(img));
        label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void enableAntialias(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private static void drawAxes(Graphics2D g, int width, int height, int padding, int legendWidth,
                                 Bounds b, double scaleX, double scaleY) {

        int left = padding;
        int right = width - padding - legendWidth;
        int bottom = height - padding;
        int top = padding;

        g.setColor(new Color(230, 233, 239));
        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0;

            double xv = b.minX() + (b.maxX() - b.minX()) * t;
            double yv = b.minY() + (b.maxY() - b.minY()) * t;

            int sx = left + (int) ((xv - b.minX()) * scaleX);
            int sy = bottom - (int) ((yv - b.minY()) * scaleY);

            g.drawLine(sx, bottom, sx, top);
            g.drawLine(left, sy, right, sy);
        }

        g.setColor(new Color(60, 60, 60));
        g.setStroke(new BasicStroke(1.7f));
        g.drawLine(left, bottom, right, bottom);
        g.drawLine(left, bottom, left, top);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0;
            double xv = b.minX() + (b.maxX() - b.minX()) * t;
            int sx = left + (int) ((xv - b.minX()) * scaleX);
            String lbl = String.format("%.2f", xv);
            int w = g.getFontMetrics().stringWidth(lbl);
            g.drawString(lbl, sx - w / 2, bottom + 20);
        }

        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0;
            double yv = b.minY() + (b.maxY() - b.minY()) * t;
            int sy = bottom - (int) ((yv - b.minY()) * scaleY);
            String lbl = String.format("%.2f", yv);
            int w = g.getFontMetrics().stringWidth(lbl);
            g.drawString(lbl, left - w - 6, sy + 4);
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("X", right + 6, bottom + 4);
        g.drawString("Y", left - 10, top - 6);
    }

    private static Map<String, Color> makePalette(List<Point> train, List<Point> tests) {
        Color[] base = {
                new Color(0x3b82f6),
                new Color(0x22c55e),
                new Color(0xf97316),
                new Color(0xef4444),
                new Color(0xa855f7),
                new Color(0x14b8a6),
                new Color(0xf59e0b),
                new Color(0x10b981)
        };

        Set<String> labels = new HashSet<>();
        for (Point p : train) {
            if (p != null && p.hasLabel()) {
                labels.add(p.getLabel());
            }
        }
        if (tests != null) {
            for (Point p : tests) {
                if (p != null && p.hasLabel()) {
                    String extracted = extractClassFromTestLabel(p.getLabel());
                    if (!extracted.isEmpty()) {
                        labels.add(extracted);
                    }
                }
            }
        }

        List<String> sorted = new ArrayList<>(labels);
        Collections.sort(sorted);

        Map<String, Color> palette = new HashMap<>();
        for (int i = 0; i < sorted.size(); i++) {
            palette.put(sorted.get(i), base[i % base.length]);
        }
        return palette;
    }

    private static String extractClassFromTestLabel(String label) {
        if (label == null) return "";
        int arrow = label.indexOf("->");
        if (arrow >= 0 && arrow + 2 < label.length()) {
            return label.substring(arrow + 2).trim();
        }
        return label.trim();
    }

    private static void drawTrainPoints(Graphics2D g, List<Point> train, Bounds b, double scaleX, double scaleY,
                                        int padding, int height, Map<String, Color> palette) {

        int bottom = height - padding;
        int left = padding;
        int size = 6;

        for (Point p : train) {
            if (p == null || !p.hasLabel()) continue;

            int sx = left + (int) ((p.getX() - b.minX()) * scaleX);
            int sy = bottom - (int) ((p.getY() - b.minY()) * scaleY);

            Color base = palette.getOrDefault(p.getLabel(), Color.GRAY);
            g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 190));
            g.fillOval(sx - size / 2, sy - size / 2, size, size);

            g.setColor(new Color(35, 35, 35, 110));
            g.setStroke(new BasicStroke(0.8f));
            g.drawOval(sx - size / 2, sy - size / 2, size, size);
        }
    }

    private static void drawTestPoints(Graphics2D g, List<Point> tests, Bounds b, double scaleX, double scaleY,
                                       int padding, int height, Map<String, Color> palette) {

        int bottom = height - padding;
        int left = padding;
        int size = 12;

        g.setFont(new Font("SansSerif", Font.BOLD, 11));

        for (int i = 0; i < tests.size(); i++) {
            Point p = tests.get(i);
            if (p == null) continue;

            String className = extractClassFromTestLabel(p.getLabel());
            Color base = palette.getOrDefault(className, Color.BLACK);

            int sx = left + (int) ((p.getX() - b.minX()) * scaleX);
            int sy = bottom - (int) ((p.getY() - b.minY()) * scaleY);

            g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 220));
            g.fillOval(sx - size / 2, sy - size / 2, size, size);

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(sx - size / 2, sy - size / 2, size, size);

            g.drawString("#" + (i + 1), sx + size / 2 + 4, sy - 1);
        }
    }

    private static void drawTitleAndLegend(Graphics2D g, int width, int padding, int legendWidth,
                                           String title, Map<String, Color> palette, boolean hasTests) {

        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(new Color(40, 40, 40));
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - legendWidth - tw) / 2, 28);

        int boxX = width - legendWidth + 10;
        int boxY = 70;
        int itemHeight = 22;

        int rows = palette.size() + (hasTests ? 1 : 0);
        int boxHeight = rows * itemHeight + 26;

        g.setColor(new Color(255, 255, 255, 240));
        g.fillRoundRect(boxX, boxY - 20, legendWidth - 20, boxHeight, 10, 10);

        g.setColor(new Color(120, 120, 120));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(boxX, boxY - 20, legendWidth - 20, boxHeight, 10, 10);

        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(new Color(45, 45, 45));
        g.drawString("Legend", boxX + 10, boxY - 4);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        List<String> labels = new ArrayList<>(palette.keySet());
        Collections.sort(labels);

        int row = 0;
        for (String lbl : labels) {
            int y = boxY + 10 + row * itemHeight;
            Color c = palette.get(lbl);

            g.setColor(c);
            g.fillRect(boxX + 10, y - 10, 14, 14);

            g.setColor(Color.DARK_GRAY);
            g.drawRect(boxX + 10, y - 10, 14, 14);
            g.drawString(lbl, boxX + 30, y + 2);
            row++;
        }

        if (hasTests) {
            int y = boxY + 10 + row * itemHeight;
            g.setColor(new Color(0, 0, 0, 120));
            g.fillOval(boxX + 10, y - 10, 14, 14);
            g.setColor(Color.BLACK);
            g.drawOval(boxX + 10, y - 10, 14, 14);
            g.drawString("Test point", boxX + 30, y + 2);
        }
    }

    private static List<Point> mergeLists(List<Point> a, List<Point> b) {
        List<Point> res = new ArrayList<>();
        if (a != null) res.addAll(a);
        if (b != null) res.addAll(b);
        return res;
    }
}