package org.example;

import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

/*To run program edit Program arguments in configuration, example: --k=50 --input=data.csv --predict=5.0,3.5 --out=result.png */
public class App {

    public static class Point {
        private final double x;
        private final double y;
        private String label;

        public Point(double x, double y, String label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    private static class Neighbor implements Comparable<Neighbor> {
        final Point point;
        final double distance;

        Neighbor(Point point, double distance) {
            this.point = point;
            this.distance = distance;
        }

        @Override
        public int compareTo(Neighbor other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public static void main(String[] args) {
        Map<String, String> options = parseCommandLineArgs(args);
        int k = Integer.parseInt(options.getOrDefault("k", "3"));
        String inputPathStr = options.getOrDefault("input", "..data.csv");
        String outputPathStr = options.getOrDefault("out", "..results.png");
        String predictStr = options.get("predict");

        Path inputPath = Paths.get(inputPathStr).toAbsolutePath();
        Path outputPath = Paths.get(outputPathStr).toAbsolutePath();

        System.out.println("Input: " + inputPath);
        System.out.println("Output: " + outputPath);
        System.out.println("k = " + k);

        List<Point> trainingPoints;
        try {
            trainingPoints = loadTrainingPoints(inputPath);
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            return;
        }

        List<Point> queryPoints = new ArrayList<>();
        if (predictStr != null) {
            String[] coords = predictStr.split(",");
            if (coords.length == 2) {
                try {
                    double x = Double.parseDouble(coords[0].trim());
                    double y = Double.parseDouble(coords[1].trim());
                    Point query = new Point(x, y, null);
                    String predictedLabel = classify(trainingPoints, query, k);
                    query.setLabel(predictedLabel);
                    queryPoints.add(query);
                    System.out.println("Predicted label for (" + x + "," + y + "): " + predictedLabel);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid --predict format. Expected: x,y");
                }
            } else {
                System.err.println("--predict must be in format 'x,y'");
            }
        }

        try {
            generateAndSavePlot(trainingPoints, queryPoints, outputPath, "KNN (k=" + k + ")");
            System.out.println("Plot saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to save image: " + e.getMessage());
        }
    }


    private static Map<String, String> parseCommandLineArgs(String[] args) {
        Map<String, String> opts = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String keyVal = arg.substring(2);
                int eq = keyVal.indexOf('=');
                if (eq >= 0) {
                    opts.put(keyVal.substring(0, eq), keyVal.substring(eq + 1));
                } else {
                    opts.put(keyVal, "true");
                }
            }
        }
        return opts;
    }

    private static List<Point> loadTrainingPoints(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Input file not found: " + path);
        }

        List<Point> points = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;

                try {
                    double x = Double.parseDouble(parts[0].trim());
                    double y = Double.parseDouble(parts[1].trim());
                    String label = parts[2].trim();
                    if (!label.isEmpty()) {
                        points.add(new Point(x, y, label));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return points;
    }

    private static String classify(List<Point> trainingSet, Point query, int k) {
        if (trainingSet.isEmpty()) {
            return "unknown";
        }

        List<Neighbor> neighbors = new ArrayList<>();
        for (Point p : trainingSet) {
            double dist = euclideanDistance(p, query);
            neighbors.add(new Neighbor(p, dist));
        }

        neighbors.sort(null);

        // Count votes among k nearest
        Map<String, Integer> voteCount = new HashMap<>();
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            String label = neighbors.get(i).point.getLabel();
            voteCount.merge(label, 1, Integer::sum);
        }

        String bestLabel = null;
        int maxVotes = -1;
        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            String label = entry.getKey();
            int votes = entry.getValue();

            if (votes > maxVotes) {
                maxVotes = votes;
                bestLabel = label;
            } else if (votes == maxVotes) {
                double currentBestDist = findNearestDistance(neighbors, bestLabel, k);
                double candidateDist = findNearestDistance(neighbors, label, k);
                if (candidateDist < currentBestDist) {
                    bestLabel = label;
                }
            }
        }

        return bestLabel != null ? bestLabel : "unknown";
    }

    private static double findNearestDistance(List<Neighbor> neighbors, String label, int k) {
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            if (label.equals(neighbors.get(i).point.getLabel())) {
                return neighbors.get(i).distance;
            }
        }
        return Double.MAX_VALUE;
    }

    private static double euclideanDistance(Point a, Point b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }


    private static void generateAndSavePlot(List<Point> training, List<Point> queries, Path outputPath, String title) throws IOException {
        KNNVisualizer visualizer = new KNNVisualizer(training, queries, title);
        BufferedImage image = visualizer.renderImage();

        Files.createDirectories(outputPath.getParent());
        ImageIO.write(image, "png", outputPath.toFile());
    }

    private static class KNNVisualizer {
        private final List<Point> trainingPoints;
        private final List<Point> queryPoints;
        private final String title;
        private final Map<String, Color> colorMap = new HashMap<>();

        private static final int WIDTH = 800;
        private static final int HEIGHT = 600;
        private static final int MARGIN = 50;
        private static final int TRAIN_POINT_SIZE = 6;
        private static final int QUERY_POINT_SIZE = 10;

        private final Color[] palette = {
                Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE,
                Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW, Color.BLACK
        };

        public KNNVisualizer(List<Point> training, List<Point> queries, String title) {
            this.trainingPoints = training;
            this.queryPoints = queries;
            this.title = title;
            buildColorMap();
        }

        private void buildColorMap() {
            Set<String> labels = new LinkedHashSet<>();
            for (Point p : trainingPoints) {
                if (p.getLabel() != null) labels.add(p.getLabel());
            }
            for (Point p : queryPoints) {
                if (p.getLabel() != null) labels.add(p.getLabel());
            }

            int index = 0;
            for (String label : labels) {
                colorMap.put(label, palette[index % palette.length]);
                index++;
            }
        }

        public BufferedImage renderImage() {
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            configureGraphics(g2d);


            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);


            Bounds bounds = calculateBounds();
            double scaleX = (WIDTH - 2 * MARGIN) / bounds.width();
            double scaleY = (HEIGHT - 2 * MARGIN) / bounds.height();


            for (Point p : trainingPoints) {
                if (p.getLabel() != null) {
                    Color color = colorMap.get(p.getLabel());
                    if (color == null) color = Color.GRAY;
                    drawPoint(g2d, p, color, TRAIN_POINT_SIZE, bounds, scaleX, scaleY);
                }
            }


            for (Point q : queryPoints) {
                if (q.getLabel() != null) {
                    Color color = colorMap.get(q.getLabel());
                    if (color == null) color = Color.BLACK;
                    drawQueryPoint(g2d, q, color, QUERY_POINT_SIZE, bounds, scaleX, scaleY);
                }
            }

            drawLegend(g2d);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString(title, WIDTH / 2 - 120, 25);

            g2d.dispose();
            return image;
        }

        private void configureGraphics(Graphics2D g) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        private void drawPoint(Graphics2D g, Point p, Color color, int size, Bounds b, double sx, double sy) {
            int x = MARGIN + (int) ((p.getX() - b.minX) * sx);
            int y = HEIGHT - MARGIN - (int) ((p.getY() - b.minY) * sy);
            g.setColor(color);
            g.fillOval(x - size / 2, y - size / 2, size, size);
        }

        private void drawQueryPoint(Graphics2D g, Point p, Color fillColor, int size, Bounds b, double sx, double sy) {
            int x = MARGIN + (int) ((p.getX() - b.minX) * sx);
            int y = HEIGHT - MARGIN - (int) ((p.getY() - b.minY) * sy);
            g.setColor(fillColor);
            g.fillOval(x - size, y - size, 2 * size, 2 * size);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(x - size, y - size, 2 * size, 2 * size);
        }

        private void drawLegend(Graphics2D g) {
            int x = WIDTH - 140;
            int y = 40;
            int lineHeight = 20;

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Legend:", x, y);

            List<String> labels = new ArrayList<>(colorMap.keySet());
            labels.sort(String::compareTo);

            g.setFont(new Font("Arial", Font.PLAIN, 12));
            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                Color color = colorMap.get(label);
                int rectY = y + 10 + i * lineHeight;
                g.setColor(color);
                g.fillRect(x, rectY, 10, 10);
                g.setColor(Color.BLACK);
                g.drawRect(x, rectY, 10, 10);
                g.drawString(label, x + 15, rectY + 9);
            }
        }

        private Bounds calculateBounds() {
            double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
            double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

            for (Point p : trainingPoints) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minY = Math.min(minY, p.getY());
                maxY = Math.max(maxY, p.getY());
            }
            for (Point p : queryPoints) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minY = Math.min(minY, p.getY());
                maxY = Math.max(maxY, p.getY());
            }

            if (minX == Double.MAX_VALUE) {
                minX = 0; maxX = 1; minY = 0; maxY = 1;
            }

            double xRange = Math.max(1e-6, maxX - minX);
            double yRange = Math.max(1e-6, maxY - minY);
            double padX = xRange * 0.1;
            double padY = yRange * 0.1;

            return new Bounds(minX - padX, maxX + padX, minY - padY, maxY + padY);
        }

        private record Bounds(double minX, double maxX, double minY, double maxY) {
            double width() { return maxX - minX; }
            double height() { return maxY - minY; }
        }
    }

    public String getGreeting() {
        return "Hello from KNN App";
    }
}