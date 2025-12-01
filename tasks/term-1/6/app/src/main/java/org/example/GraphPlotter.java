package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class GraphPlotter {
    private List<Point> points;

    public GraphPlotter(List<Point> points) {
        this.points = points;
    }

    public void display() {
        JFrame frame = new JFrame("KNN Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        GraphPanel graphPanel = new GraphPanel(points);
        frame.add(graphPanel);
        frame.setVisible(true);
    }


    public static class GraphPanel extends JPanel {
        private List<Point> points;
        private Map<String, Color> colorMap;

        public GraphPanel(List<Point> points) {
            this.points = points;
            this.colorMap = createColorMap();
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(700, 500));
        }

        private Map<String, Color> createColorMap() {
            Map<String, Color> colors = new HashMap<>();
            colors.put("A", Color.RED);
            colors.put("B", Color.BLUE);
            colors.put("C", Color.GREEN);

            int colorIndex = 0;
            Color[] defaultColors = {Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW};

            for (Point p : points) {
                if (p.getLabel() != null && !colors.containsKey(p.getLabel())) {
                    colors.put(p.getLabel(), defaultColors[colorIndex % defaultColors.length]);
                    colorIndex++;
                }
            }
            return colors;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int width = getWidth();
            int height = getHeight();
            int margin = 40;

            double minX = points.stream().mapToDouble(Point::getX).min().orElse(0);
            double maxX = points.stream().mapToDouble(Point::getX).max().orElse(100);
            double minY = points.stream().mapToDouble(Point::getY).min().orElse(0);
            double maxY = points.stream().mapToDouble(Point::getY).max().orElse(100);

            double xScale = (width - 2 * margin) / (maxX - minX);
            double yScale = (height - 2 * margin) / (maxY - minY);

            g.setColor(Color.BLACK);
            g.drawLine(margin, height - margin, width - margin, height - margin); // X
            g.drawLine(margin, margin, margin, height - margin); // Y

            for (Point point : points) {
                int x = margin + (int) ((point.getX() - minX) * xScale);
                int y = height - margin - (int) ((point.getY() - minY) * yScale);

                Color pointColor = colorMap.getOrDefault(point.getLabel(), Color.GRAY);
                g.setColor(pointColor);
                g.fillOval(x - 4, y - 4, 8, 8);
                g.setColor(Color.BLACK);
                g.drawOval(x - 4, y - 4, 8, 8);
            }

            drawLegend(g, width);
        }

        private void drawLegend(Graphics g, int width) {
            int x = width - 120;
            int y = 30;
            int lineHeight = 15;

            g.setColor(Color.BLACK);
            g.drawString("Легенда:", x, y);

            Set<String> labels = new TreeSet<>(colorMap.keySet());
            int i = 0;
            for (String label : labels) {
                g.setColor(colorMap.get(label));
                g.fillRect(x, y + 20 + i * lineHeight, 10, 10);
                g.setColor(Color.BLACK);
                g.drawString(label, x + 15, y + 30 + i * lineHeight);
                i++;
            }
        }
    }
}
