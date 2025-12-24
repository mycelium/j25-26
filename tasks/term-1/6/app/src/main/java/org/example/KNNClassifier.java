package org.example;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class KNNClassifier {

    static class Dot {
        double x, y;
        String clazz; // класс (метка)

        Dot(double x, double y, String clazz) {
            this.x = x;
            this.y = y;
            this.clazz = clazz;
        }
    }

    // Класс для хранения ближайшего соседа
    static class Closest {
        Dot dot;
        double distance;

        Closest(Dot dot, double dist) {
            this.dot = dot;
            this.distance = dist;
        }
    }

    public static void main(String[] args) {
        // Разбор аргументов командной строки
        int k = 3;
        String inputPathStr = "points.csv";
        String outputPathStr = "plot.png";
        String queryPointStr = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--k") && i + 1 < args.length) {
                k = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--input") && i + 1 < args.length) {
                inputPathStr = args[++i];
            } else if (args[i].equals("--out") && i + 1 < args.length) {
                outputPathStr = args[++i];
            } else if (args[i].equals("--predict") && i + 1 < args.length) {
                queryPointStr = args[++i];
            }
        }

        Path inputPath = Paths.get(inputPathStr).toAbsolutePath();
        Path outputPath = Paths.get(outputPathStr).toAbsolutePath();

        System.out.println("Загрузка данных из: " + inputPath);
        System.out.println("k = " + k);

        
        List<Dot> trainSet;
        try {
            trainSet = loadDots(inputPath);
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return;
        }

        
        List<Dot> allDots = new ArrayList<>(trainSet);
        List<Dot> newDots = new ArrayList<>();

       
        if (queryPointStr != null) {
            String[] parts = queryPointStr.split(",");
            if (parts.length == 2) {
                try {
                    double x = Double.parseDouble(parts[0].trim());
                    double y = Double.parseDouble(parts[1].trim());
                    Dot newDot = new Dot(x, y, null);

                    String predictedClass = predictClass(trainSet, newDot, k);
                    newDot.clazz = predictedClass;
                    newDots.add(newDot);
                    allDots.add(newDot);

                    System.out.println("Предсказано: (" + x + ", " + y + ") → " + predictedClass);
                } catch (NumberFormatException e) {
                    System.err.println("Некорректная точка: " + queryPointStr);
                }
            } else {
                System.err.println("Формат точки: x,y");
            }
        }

        
        try {
            drawAndSave(allDots, newDots, outputPath, "KNN (k=" + k + ")");
            System.out.println("График сохранён: " + outputPath);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения изображения: " + e.getMessage());
        }
    }

   
    static List<Dot> loadDots(Path path) throws IOException {
        List<Dot> dots = new ArrayList<>();
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Файл не найден: " + path);
        }

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
                    String clazz = cols[2].trim();
                    dots.add(new Dot(x, y, clazz));
                } catch (NumberFormatException ignored) {
                   
                }
            }
        }
        return dots;
    }

    
    static String predictClass(List<Dot> train, Dot target, int k) {
        List<Closest> distances = new ArrayList<>();
        for (Dot d : train) {
            double dist = Math.sqrt(Math.pow(d.x - target.x, 2) + Math.pow(d.y - target.y, 2));
            distances.add(new Closest(d, dist));
        }

        
        distances.sort(Comparator.comparingDouble(c -> c.distance));

    
        Map<String, Integer> votes = new HashMap<>();
        int limit = Math.min(k, distances.size());
        for (int i = 0; i < limit; i++) {
            String cls = distances.get(i).dot.clazz;
            votes.put(cls, votes.getOrDefault(cls, 0) + 1);
        }

        
        String bestClass = null;
        int maxVotes = -1;
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                bestClass = entry.getKey();
            }
        }
        return bestClass;
    }

    static void drawAndSave(List<Dot> all, List<Dot> newDots, Path outputPath, String title) throws IOException {
        int width = 800, height = 600;
        int margin = 50, pointRadius = 6, newPointRadius = 10;

        double minX = all.stream().mapToDouble(d -> d.x).min().orElse(0.0);
        double maxX = all.stream().mapToDouble(d -> d.x).max().orElse(1.0);
        double minY = all.stream().mapToDouble(d -> d.y).min().orElse(0.0);
        double maxY = all.stream().mapToDouble(d -> d.y).max().orElse(1.0);

        double dx = maxX - minX, dy = maxY - minY;
        minX -= dx * 0.1; maxX += dx * 0.1;
        minY -= dy * 0.1; maxY += dy * 0.1;
        if (dx == 0) dx = 1;
        if (dy == 0) dy = 1;

        double scaleX = (width - 2 * margin) / dx;
        double scaleY = (height - 2 * margin) / dy;

        Map<String, Color> colorMap = new HashMap<>();
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK};
        Set<String> classes = new LinkedHashSet<>();
        for (Dot d : all) {
            if (d.clazz != null) classes.add(d.clazz);
        }
        int idx = 0;
        for (String c : classes) {
            colorMap.put(c, colors[idx++ % colors.length]);
        }

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        for (Dot d : all) {
            if (d.clazz == null) continue;
            Color col = colorMap.get(d.clazz);
            int px = margin + (int) ((d.x - minX) * scaleX);
            int py = height - margin - (int) ((d.y - minY) * scaleY);
            g.setColor(col);
            g.fillOval(px - pointRadius, py - pointRadius, 2 * pointRadius, 2 * pointRadius);
        }

        g.setStroke(new BasicStroke(2));
        for (Dot d : newDots) {
            if (d.clazz == null) continue;
            Color col = colorMap.getOrDefault(d.clazz, Color.BLACK);
            int px = margin + (int) ((d.x - minX) * scaleX);
            int py = height - margin - (int) ((d.y - minY) * scaleY);
            g.setColor(col);
            g.fillOval(px - newPointRadius, py - newPointRadius, 2 * newPointRadius, 2 * newPointRadius);
            g.setColor(Color.BLACK);
            g.drawOval(px - newPointRadius, py - newPointRadius, 2 * newPointRadius, 2 * newPointRadius);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(title, width / 2 - 100, 30);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        int lx = width - 140, ly = 50;
        g.drawString("Классы:", lx, ly);
        int line = 0;
        List<String> sorted = new ArrayList<>(colorMap.keySet());
        Collections.sort(sorted);
        for (String cls : sorted) {
            Color c = colorMap.get(cls);
            g.setColor(c);
            g.fillRect(lx, ly + 20 + line * 20, 12, 12);
            g.setColor(Color.BLACK);
            g.drawRect(lx, ly + 20 + line * 20, 12, 12);
            g.drawString(cls, lx + 20, ly + 30 + line * 20);
            line++;
        }

        g.dispose();

        Files.createDirectories(outputPath.getParent());
        ImageIO.write(img, "png", outputPath.toFile());
    }
}
