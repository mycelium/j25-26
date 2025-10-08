package org.example.Processer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class Processer<T>
{
    public List<T> processCsv(String path, Function<String, T> analyzer) throws FileNotFoundException
    {
        File file = new File(path);
        if (!file.isFile()) 
            throw new FileNotFoundException(String.format("No such file: %s", path));

        var reader = new BufferedReader(new FileReader(path));
        
        var results = new ArrayList<T>();

        String line;
        try {
            while ((line = reader.readLine()) != null) 
            {
                results.add(analyzer.apply(line));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return results;
    }
}
