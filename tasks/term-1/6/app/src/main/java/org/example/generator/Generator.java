package org.example.generator;

import org.example.grid.Point;

import java.util.List;
import java.util.Random;

public interface Generator {

    static public Random r = new Random();

    public String      getName();
    public List<Point> generate(int xLength, int yLength);
}
