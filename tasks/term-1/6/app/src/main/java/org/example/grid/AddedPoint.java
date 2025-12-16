package org.example.grid;

import java.util.ArrayList;
import java.util.List;

public class AddedPoint extends Point {

    private List<Point> surrounding;

    public AddedPoint(int aX, int aY, Point.ClassLabel aClsLabel,
                      List<Point> aSurrounding){
        super(aX,aY,aClsLabel);
        surrounding = aSurrounding;
    }

    public List<Point> getSurrounding(){
        return new ArrayList<>(surrounding);
    };
}
