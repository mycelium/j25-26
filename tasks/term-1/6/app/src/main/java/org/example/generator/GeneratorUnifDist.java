package org.example.generator;

import org.example.grid.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneratorUnifDist implements Generator {

    @Override
    public String getName(){
        return "Равномерное распределение";
    }

    @Override
    public List<Point> generate(int xLength, int yLength) {
        if (Point.ClassLabel.values().length == 0)
            return new ArrayList<>();
        int         totalPoints    = (xLength + yLength) / 2;
        int         pointsPerClass = totalPoints /
                                     Point.ClassLabel.values().length;
        List<Point> res            = new ArrayList<>(totalPoints);

        for (int i = 0; i < Point.ClassLabel.values().length; i++) {
            for (int j = 0; j < pointsPerClass; j++) {
                int x = (int) (r.nextDouble() * xLength);
                int y = (int) (r.nextDouble() * yLength);
                res.add(new Point(x, y, Point.ClassLabel.values()[i]));
            }
        }
        return res;
    }
}
