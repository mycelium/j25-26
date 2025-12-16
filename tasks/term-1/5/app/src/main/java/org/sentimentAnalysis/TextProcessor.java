package org.sentimentAnalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TextProcessor {

    public List<Entry> parseData(String path) throws IOException {
        List<Entry> entries = new ArrayList<>();
        List<String> rawLines = Files.readAllLines(Paths.get(path));

        for (int i = 1; i < rawLines.size(); ++i) {
            String rawLine = rawLines.get(i).trim();
            if (rawLine.isEmpty()) continue;

            String[] fragments = rawLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (fragments.length < 2) continue;

            String rawText = fragments[0].replaceAll("\"", "").trim();
            String rawLabel = fragments[1].replaceAll("\"", "").trim();

            String sanitizedText = sanitize(rawText);
            entries.add(new Entry(sanitizedText, rawLabel));
        }
        return entries;
    }

    private String sanitize(String input) {
        return input.replace("<br />", " ").trim();
    }

    public static class Entry {
        private final String content;
        private final String label;

        public Entry(String content, String label) {
            this.content = content;
            this.label = label;
        }

        public String getContent() {
            return content;
        }

        public String getLabel() {
            return label;
        }
    }
}