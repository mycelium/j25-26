package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewReader {

    public List<String> load(InputStream input) {
        List<String> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {

            String line;
            boolean skipHeader = true;

            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.contains(",")) {
                    if (skipHeader) {
                        skipHeader = false;
                        continue;
                    }

                    int comma = line.indexOf(',');
                    String review = line.substring(0, comma).replace("\"", "").trim();
                    if (!review.isEmpty()) list.add(review);

                } else {
                    list.add(line);
                }
            }

        } catch (Exception ex) {
            System.out.println("Error reading file: " + ex.getMessage());
        }

        return list;
    }
}
