package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Processor {

    public List<Entry> parseData(String fileName) {
        List<Entry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream(fileName),
                        StandardCharsets.UTF_8))) {

            if (reader == null) {
                throw new IOException("Файл '" + fileName + "' не найден в ресурсах.");
            }

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = splitCSVLine(line);
                if (parts.length < 2) continue;

                String review = parts[0].replaceAll("^\"|\"$", "").trim();
                String label = parts[1].replaceAll("^\"|\"$", "").trim();

                review = review.replace("<br />", " ").trim();

                entries.add(new Entry(review, label));
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла: " + e.getMessage(), e);
        }
        return entries;
    }

    private String[] splitCSVLine(String line) {
        return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    public static class Entry {
        private final String content;
        private final String label;

        public Entry(String content, String label) {
            this.content = content;
            this.label = label;
        }

        public String getContent() { return content; }
        public String getLabel() { return label; }
    }
}