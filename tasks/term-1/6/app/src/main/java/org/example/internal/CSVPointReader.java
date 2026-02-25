package org.example.internal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

public class CSVPointReader {
    
    public List<Point> readPointsFromCSV(String filename) {
        
        List<Point> list = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                
                String[] coordinates = line.split(";");
                list.add(new Point(
                    Double.parseDouble(coordinates[0]),
                    Double.parseDouble(coordinates[1]),
                    coordinates[2]
                ));
            }

        } catch (Exception e) {
            
            throw new RuntimeErrorException(null);
        }

        return list;
    }
}