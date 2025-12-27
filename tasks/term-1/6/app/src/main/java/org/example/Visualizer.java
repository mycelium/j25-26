package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Visualizer {
    private final List<Point> dataPoints;

    public Visualizer(List<Point> points) {
        this.dataPoints = new ArrayList<>(points);
    }

    public void showWindow() {
        JFrame window = new JFrame("визуализация классов");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(720, 520);

        DisplayPanel panel = new DisplayPanel(dataPoints);
        window.add(panel);
        window.setVisible(true);
    }

    private static class DisplayPanel extends JPanel {
        private final List<Point> items;
        private final Map<String, Color> palette;

        public DisplayPanel(List<Point> points) {
            this.items = points;
            this.palette = buildColorPalette();
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(720, 520));
        }

        private Map<String, Color> buildColorPalette() {
            Map<String, Color> map = new HashMap<>();
            map.put("A", new Color(255, 100, 100));
            map.put("B", new Color(100, 100, 255));
            map.put("C", new Color(100, 255, 100));

            Color[] extra = {Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK};
            int idx = 0;
            for (Point p : items) {
                String label = p.getLabel();
                if (label != null && !map.containsKey(label)) {
                    map.put(label, extra[idx % extra.length]);
                    idx++;
                }
            }
            return map;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int margin = 45;

            double minX = items.stream().mapToDouble(Point::getX).min().orElse(0);
            double maxX = items.stream().mapToDouble(Point::getX).max().orElse(100);
            double minY = items.stream().mapToDouble(Point::getY).min().orElse(0);
            double maxY = items.stream().mapToDouble(Point::getY).max().orElse(100);

            double rangeX = maxX - minX;
            double rangeY = maxY - minY;
            double scaleX = (width - 2 * margin) / rangeX;
            double scaleY = (height - 2 * margin) / rangeY;

            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(margin, height - margin, width - margin, height - margin);
            g.drawLine(margin, margin, margin, height - margin);

            g.setColor(Color.BLACK);
            double step = 10.0;
            for (double xVal = Math.ceil(minX / step) * step; xVal <= maxX; xVal += step) {
                int screenX = margin + (int) ((xVal - minX) * scaleX);
                g.drawLine(screenX, height - margin - 3, screenX, height - margin + 3);
                String label = String.valueOf((int) xVal);
                g.drawString(label, screenX - g.getFontMetrics().stringWidth(label) / 2, height - margin + 15);
            }

            for (double yVal = Math.ceil(minY / step) * step; yVal <= maxY; yVal += step) {
                int screenY = height - margin - (int) ((yVal - minY) * scaleY);
                g.drawLine(margin - 3, screenY, margin + 3, screenY);
                String label = String.valueOf((int) yVal);
                g.drawString(label, margin - g.getFontMetrics().stringWidth(label) - 5, screenY + 4);
            }

            for (Point p : items) {
                int px = margin + (int) ((p.getX() - minX) * scaleX);
                int py = height - margin - (int) ((p.getY() - minY) * scaleY);

                Color c = palette.getOrDefault(p.getLabel(), Color.GRAY);
                g.setColor(c);
                g.fillOval(px - 5, py - 5, 10, 10);
                g.setColor(Color.BLACK);
                g.drawOval(px - 5, py - 5, 10, 10);
            }

            drawLegend(g, width);
        }

        private void drawLegend(Graphics g, int panelWidth) {
            int x = 75;
            int y = 50;
            g.setColor(Color.BLACK);
            g.drawString("классы:", x, y);

            Set<String> labels = new TreeSet<>(palette.keySet());
            int i = 0;
            for (String label : labels) {
                g.setColor(palette.get(label));
                g.fillRect(x, y + 20 + i * 18, 12, 12);
                g.setColor(Color.BLACK);
                g.drawString(label, x + 18, y + 30 + i * 18);
                i++;
            }
        }
    }
}