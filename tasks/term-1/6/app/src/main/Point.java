public class Point {
    private double x;
    private double y;
    private String label;

    public Point(double x, double y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public Point(double x, double y) {
        this(x, y, null);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + ", label='" + label + '\'' + '}';
    }
}

