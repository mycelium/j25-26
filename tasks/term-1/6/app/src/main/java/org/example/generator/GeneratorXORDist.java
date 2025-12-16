package org.example.generator;

import org.example.grid.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneratorXORDist implements Generator {

    @Override
    public String getName(){
        return "XOR распределение";
    }

    private Point getRandomPoint(int quarterSumX,  int quarterSumY,
                                         int quarterMultX, int quarterMultY,
                                         Point.ClassLabel clsLabel){
        return new Point((int)(quarterSumX + r.nextGaussian() * quarterMultX * 0.25),
                         (int)(quarterSumY + r.nextGaussian() * quarterMultY * 0.25),
                         clsLabel);
    }

    @Override
    public List<Point> generate(int xLength, int yLength) {
        if (Point.ClassLabel.values().length < 2)
            return new ArrayList<>();
        int         totalPoints    = (xLength + yLength) / 2;
        int         classesCount   = 2;
        int         pointsPerClass = totalPoints / classesCount;
        List<Point> res            = new ArrayList<>(totalPoints);

        int quarterX      = xLength / 4,  quarterY      = yLength / 4;
        int thirdQuarterX = 3 * xLength / 4;
        int thirdQuarterY = 3 * yLength / 4;
        for (int i = 0;  i < pointsPerClass / 2; i++)
        {
            res.add(getRandomPoint(quarterX, quarterY, quarterX, quarterY,
                                   Point.ClassLabel.values()[0]));
            res.add(getRandomPoint(thirdQuarterX, thirdQuarterY, quarterX, quarterY,
                                   Point.ClassLabel.values()[0]));
            res.add(getRandomPoint(thirdQuarterX, quarterY, quarterX, quarterY,
                                   Point.ClassLabel.values()[1]));
            res.add(getRandomPoint(quarterX, thirdQuarterY, quarterX, quarterY,
                                   Point.ClassLabel.values()[1]));
        }
        return res;
    }

}

/*
   @Override
    public List<Point> generate(int xLength, int yLength) {
        int totalPoints = 250;
        int classesCount = 2; // Для XOR нужно 2 класса
        int pointsPerClass = totalPoints / classesCount;
        List<Point> res = new ArrayList<>(totalPoints);
        Random random = new Random();

        int quarterX = xLength / 4;
        int quarterY = yLength / 4;
        int threeQuarterX = 3 * xLength / 4;
        int threeQuarterY = 3 * yLength / 4;

        // Класс 0: левый верхний и правый нижний квадранты
        // Класс 1: правый верхний и левый нижний квадранты

        for (int i = 0; i < pointsPerClass / 2; i++) {
            // Левая верхняя группа (класс 0)
            int x1 = (int) (quarterX + random.nextGaussian() * quarterX * 0.3);
            int y1 = (int) (quarterY + random.nextGaussian() * quarterY * 0.3);
            res.add(new Point(x1, y1, Point.ClassLabel.values()[0]));

            // Правая нижняя группа (класс 0)
            int x2 = (int) (threeQuarterX + random.nextGaussian() * quarterX * 0.3);
            int y2 = (int) (threeQuarterY + random.nextGaussian() * quarterY * 0.3);
            res.add(new Point(x2, y2, Point.ClassLabel.values()[0]));
        }

        for (int i = 0; i < pointsPerClass / 2; i++) {
            // Правая верхняя группа (класс 1)
            int x1 = (int) (threeQuarterX + random.nextGaussian() * quarterX * 0.3);
            int y1 = (int) (quarterY + random.nextGaussian() * quarterY * 0.3);
            res.add(new Point(x1, y1, Point.ClassLabel.values()[1]));

            // Левая нижняя группа (класс 1)
            int x2 = (int) (quarterX + random.nextGaussian() * quarterX * 0.3);
            int y2 = (int) (threeQuarterY + random.nextGaussian() * quarterY * 0.3);
            res.add(new Point(x2, y2, Point.ClassLabel.values()[1]));
        }

        return res;
    }
 */
