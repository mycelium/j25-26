package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;


/**
 *
 * Примеры использования
 * .\gradlew.bat run --args="--k=12 --input=../data/points.csv --predict=5.0,3.1 --out=../results/plot.png"
 * или так
 *.\gradlew.bat run --args="--k=2 --input=../data/points.csv --predict=5.0,3.1 --out=../results/plot.png"
 *
 */
public class App {

    public static class Point {
        private final double x;
        private final double y;
        private String label; // может быть null для непомеченных

        public Point(double x, double y, String label) { this.x = x; this.y = y; this.label = label; }
        public double getX() { return x; }
        public double getY() { return y; }
        public String getLabel() { return label; }
        public void setLabel(String l) { this.label = l; }
    }


    public static void main(String[] args) {
        Map<String,String> opts = parseArgs(args);
        int k = Integer.parseInt(opts.getOrDefault("k","3"));
        String input = opts.getOrDefault("input","../data/points.csv");
        String out = opts.getOrDefault("out","../results/plot.png");
        String predict = opts.get("predict"); // "x,y"

        Path inputPath = Paths.get(input).toAbsolutePath();
        Path outPath = Paths.get(out).toAbsolutePath();

        System.out.println("Input: " + inputPath);
        System.out.println("Output: " + outPath);
        System.out.println("k = " + k);

        List<Point> train;
        try {
            train = readPoints(inputPath);
        } catch (IOException e) {
            System.err.println("Ошибка чтения input: " + e.getMessage());
            return;
        }

        List<Point> testPoints = new ArrayList<>();
        if (predict != null) {
            String[] p = predict.split(",");
            if (p.length == 2) {
                try {
                    double qx = Double.parseDouble(p[0].trim());
                    double qy = Double.parseDouble(p[1].trim());
                    Point q = new Point(qx, qy, null);
                    String predicted = classifing(train, q, k);
                    q.setLabel(predicted);
                    testPoints.add(q);
                    System.out.println("Predicted label for ("+qx+","+qy+"): " + predicted);
                } catch (NumberFormatException ex) {
                    System.err.println("Bad predict argument: " + predict);
                }
            } else {
                System.err.println("predict must be x,y");
            }
        }


        try {
            saveVisualization(train, testPoints, outPath, "KNN Visualization (k=" + k + ")");
            System.out.println("Saved plot to " + outPath.toString());
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }


    private static List<Point> readPoints(Path path) throws IOException {
        List<Point> pts = new ArrayList<>();
        if (!Files.exists(path)) throw new FileNotFoundException(path.toString());
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] cols = line.split(",");
                if (cols.length < 3) continue;
                try {
                    double x = Double.parseDouble(cols[0].trim());
                    double y = Double.parseDouble(cols[1].trim());
                    String label = cols[2].trim();
                    pts.add(new Point(x,y,label));
                } catch (NumberFormatException ex) {

                }
            }
        }
        return pts;
    }


    private static String classifing(List<Point> train, Point q, int k) {
        List<Neighbor> neighbors = new ArrayList<>();
        for (Point p : train) {
            double d = distance(p, q);
            neighbors.add(new Neighbor(p, d));
        }
        Collections.sort(neighbors, Comparator.comparingDouble(n -> n.dist));
        Map<String,Integer> counts = new HashMap<>();
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            String lbl = neighbors.get(i).p.getLabel();
            counts.put(lbl, counts.getOrDefault(lbl,0) + 1);
        }

        int bestCount = -1;
        String bestLabel = null;
        for (Map.Entry<String,Integer> e : counts.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                bestLabel = e.getKey();
            } else if (e.getValue() == bestCount) {

                double distBest = nearestDistanceAmongK(neighbors, bestLabel, k);
                double distOther = nearestDistanceAmongK(neighbors, e.getKey(), k);
                if (distOther < distBest) bestLabel = e.getKey();
            }
        }
        return bestLabel;
    }

    private static double nearestDistanceAmongK(List<Neighbor> neighbors, String label, int k) {
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            if (neighbors.get(i).p.getLabel().equals(label)) return neighbors.get(i).dist;
        }
        return Double.MAX_VALUE;
    }

    private static double distance(Point a, Point b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }

    private static class Neighbor { Point p; double dist; Neighbor(Point p,double d){this.p=p;this.dist=d;} }


    public static class Canvas extends JPanel {
        private List<Point> points;
        private List<Point> testPoints;
        private static final int WIDTH = 800;
        private static final int HEIGHT = 600;
        private static final int MARGIN = 50;
        private static final int POINT_SIZE = 8;

        private Map<String, Color> colorMap;

        public Canvas(List<Point> points, List<Point> testPoints) {
            this.points = points;
            this.testPoints = testPoints;
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            initializeColorMap();
        }

        private void initializeColorMap() {
            colorMap = new HashMap<>();
            Color[] baseColors = {
                    Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW, Color.BLACK
            };

            Set<String> allLabels = new LinkedHashSet<>();
            for (Point p : points) {
                if (p.getLabel() != null) allLabels.add(p.getLabel());
            }
            for (Point p : testPoints) {
                if (p.getLabel() != null) allLabels.add(p.getLabel());
            }

            int colorIndex = 0;
            for (String label : allLabels) {
                colorMap.put(label, baseColors[colorIndex % baseColors.length]);
                colorIndex++;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
            double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

            for (Point p : points) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minY = Math.min(minY, p.getY());
                maxY = Math.max(maxY, p.getY());
            }
            for (Point p : testPoints) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minY = Math.min(minY, p.getY());
                maxY = Math.max(maxY, p.getY());
            }

            if (minX == Double.MAX_VALUE) { minX = 0; maxX = 1; minY = 0; maxY = 1; }

            double xRange = maxX - minX;
            double yRange = maxY - minY;
            if (xRange == 0) xRange = 1;
            if (yRange == 0) yRange = 1;
            minX -= xRange * 0.1;
            maxX += xRange * 0.1;
            minY -= yRange * 0.1;
            maxY += yRange * 0.1;

            double scaleX = (WIDTH - 2 * MARGIN) / (maxX - minX);
            double scaleY = (HEIGHT - 2 * MARGIN) / (maxY - minY);

            for (Point p : points) {
                if (p.getLabel() != null) {
                    g2d.setColor(colorMap.get(p.getLabel()));
                    int x = MARGIN + (int) ((p.getX() - minX) * scaleX);
                    int y = HEIGHT - MARGIN - (int) ((p.getY() - minY) * scaleY);
                    g2d.fillOval(x - POINT_SIZE/2, y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
                }
            }

            // тестовые точки
            for (Point p : testPoints) {
                if (p.getLabel() != null) {
                    Color c = colorMap.get(p.getLabel());
                    if (c == null) c = Color.BLACK;
                    g2d.setColor(c);
                    int x = MARGIN + (int) ((p.getX() - minX) * scaleX);
                    int y = HEIGHT - MARGIN - (int) ((p.getY() - minY) * scaleY);
                    g2d.fillOval(x - POINT_SIZE, y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2);
                    g2d.setColor(Color.BLACK);
                    g2d.drawOval(x - POINT_SIZE, y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2);
                }
            }

            showLegend(g2d);

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("KNN Classification Visualization", WIDTH/2 - 150, 24);
        }

        private void showLegend(Graphics2D g2d) {
            int legendX = getWidth() - 140;
            int legendY = 30;
            int lineHeight = 20;

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Legend:", legendX, legendY);

            List<String> sortedLabels = new ArrayList<>(colorMap.keySet());
            Collections.sort(sortedLabels);

            int index = 0;
            for (String label : sortedLabels) {
                Color color = colorMap.get(label);
                g2d.setColor(color);
                g2d.fillRect(legendX, legendY + 10 + index * lineHeight, 10, 10);

                g2d.setColor(Color.BLACK);
                g2d.drawRect(legendX, legendY + 10 + index * lineHeight, 10, 10);

                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.drawString(label, legendX + 15, legendY + 20 + index * lineHeight);
                index++;
            }
        }

        public static void displayVisualization(List<Point> points, List<Point> testPoints, String title) {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame(title);
                Canvas visualizer = new Canvas(points, testPoints);
                frame.add(visualizer);
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        }
    }
    public String getGreeting() {
        return "Hello from App";
    }


    private static void saveVisualization(List<Point> points, List<Point> testPoints, Path outPath, String title) throws IOException {
        Canvas canvas = new Canvas(points, testPoints);
        canvas.setSize(canvas.getPreferredSize());


        BufferedImage img = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        canvas.paint(g2d);
        g2d.dispose();


        Files.createDirectories(outPath.getParent());
        ImageIO.write(img, "png", outPath.toFile());
    }


    private static Map<String,String> parseArgs(String[] args) {
        Map<String,String> map = new HashMap<>();
        for (String a : args) {
            if (!a.startsWith("--")) continue;
            String s = a.substring(2);
            int eq = s.indexOf('=');
            if (eq >= 0) map.put(s.substring(0,eq), s.substring(eq+1));
            else map.put(s, "true");
        }
        return map;
    }
}
