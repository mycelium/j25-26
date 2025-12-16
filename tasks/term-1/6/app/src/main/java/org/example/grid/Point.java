package org.example.grid;

import java.awt.*;

public class Point{
    private int        x;
    private int        y;
    private ClassLabel clsLabel;

    public static enum ClassLabel{
        CLASS1,
        CLASS2,
        CLASS3,
        CLASS4;
    }

    public Point(int aX, int aY, ClassLabel aClsLabel){
        x        = aX;
        y        = aY;
        clsLabel = aClsLabel;
    }

    public int        getX()        { return x;        }
    public int        getY()        { return y;        }
    public ClassLabel getClsLabel() { return clsLabel; }

    public void setColor(ClassLabel aClsLabel) { clsLabel = aClsLabel; }

    public Color getColor() {
        return switch(clsLabel){
            case CLASS1 -> Color.RED;
            case CLASS2 -> Color.BLUE;
            case CLASS3 -> Color.GREEN;
            case CLASS4 -> Color.YELLOW;
            default     -> Color.WHITE;
        };
    }

    public static double distBetween(Point p, int xPos, int yPos){
        double dx = p.getX() - xPos;
        double dy = p.getY() - yPos;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

