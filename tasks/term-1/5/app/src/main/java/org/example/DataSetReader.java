package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataSetReader {

    private CSVReader reader;

    public DataSetReader(String fileName) throws IOException {
        reader = new CSVReader(new FileReader(fileName));
        reader.skip(1);
    }

    private Review readNext() throws IOException, CsvValidationException {
        String[] line = reader.readNext();
        if (line == null || line.length < 2) {
            return null;
        }

        return new Review(
                line[0].replace("<br />", " ").trim(),
                line[1]
        );
    }

    public List<Review> readAll() throws IOException, CsvValidationException {
        List<Review> all = new ArrayList<>();
        Review review;

        while ((review = readNext()) != null) {
            all.add(review);
        }
        return all;
    }

    public void close() throws IOException {
        reader.close();
    }
}

record Review(String revText, String revGrade) {}
