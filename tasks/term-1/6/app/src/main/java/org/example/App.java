package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.Graphics2D;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Примеры использования
 * .\gradlew.bat run --args="--k=12 --input=../data/points.csv --predict=5.0,3.1 --out=../results/plot.png"
 * или так
 * .\gradlew.bat run --args="--k=2 --input=../data/points.csv --predict=5.0,3.1 --out=../results/plot.png"
 */
public class App {

    public static void main(String[] args) {
        Map<String, String> opts = parseArgs(args);
        int k = Integer.parseInt(opts.getOrDefault("k", "3"));
        String input = opts.getOrDefault("input", "../data/points.csv");
        String out = opts.getOrDefault("out", "../results/plot.png");
        String predict = opts.get("predict"); // "x,y"

        Path inputPath = Paths.get(input).toAbsolutePath();
        Path outPath = Paths.get(out).toAbsolutePath();

        System.out.println("Input: " + inputPath);
        System.out.println("Output: " + outPath);
        System.out.println("k = " + k);

        java.util.List<Point> train;
        try {
            train = readPoints(inputPath);
        } catch (IOException e) {
            System.err.println("Ошибка чтения input: " + e.getMessage());
            return;
        }

        java.util.List<Point> testPoints = new ArrayList<>();
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
                    System.out.println("Predicted label for (" + qx + "," + qy + "): " + predicted);
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

    private static java.util.List<Point> readPoints(Path path) throws IOException {
        java.util.List<Point> pts = new ArrayList<>();
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
                    pts.add(new Point(x, y, label));
                } catch (NumberFormatException ex) {
                    // skip malformed lines
                }
            }
        }
        return pts;
    }

    private static String classifing(java.util.List<Point> train, Point q, int k) {
        java.util.List<Neighbor> neighbors = new ArrayList<>();
        for (Point p : train) {
            double d = distance(p, q);
            neighbors.add(new Neighbor(p, d));
        }
        Collections.sort(neighbors, Comparator.comparingDouble(n -> n.dist));
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            String lbl = neighbors.get(i).p.getLabel();
            counts.put(lbl, counts.getOrDefault(lbl, 0) + 1);
        }

        int bestCount = -1;
        String bestLabel = null;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
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

    private static double nearestDistanceAmongK(java.util.List<Neighbor> neighbors, String label, int k) {
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            if (neighbors.get(i).p.getLabel().equals(label)) return neighbors.get(i).dist;
        }
        return Double.MAX_VALUE;
    }

    private static double distance(Point a, Point b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String a : args) {
            if (!a.startsWith("--")) continue;
            String s = a.substring(2);
            int eq = s.indexOf('=');
            if (eq >= 0) map.put(s.substring(0, eq), s.substring(eq + 1));
            else map.put(s, "true");
        }
        return map;
    }

    private static void saveVisualization(
            java.util.List<Point> points,
            java.util.List<Point> testPoints,
            Path outPath,
            String title) throws IOException {
        Canvas canvas = new Canvas(points, testPoints);
        canvas.setSize(canvas.getPreferredSize());

        BufferedImage img = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        canvas.paint(g2d);
        g2d.dispose();

        Files.createDirectories(outPath.getParent());
        ImageIO.write(img, "png", outPath.toFile());
    }

    public String getGreeting() {
        return "Hello from App";
    }
}