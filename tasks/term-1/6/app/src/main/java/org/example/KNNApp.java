package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KNNApp {

    public static void main(String[] args) throws IOException {
        // создаём обучающий набор из 3 классов
        List<LabeledPoint> points = new ArrayList<>();

        Random rnd = new Random(42);

        // класс A (красный кластер)
        for (int i = 0; i < 30; i++) {
            points.add(new LabeledPoint(
                    1 + rnd.nextGaussian(),
                    1 + rnd.nextGaussian(),
                    "A"
            ));
        }

        // класс B
        for (int i = 0; i < 30; i++) {
            points.add(new LabeledPoint(
                    5 + rnd.nextGaussian(),
                    1 + rnd.nextGaussian(),
                    "B"
            ));
        }

        // класс C
        for (int i = 0; i < 30; i++) {
            points.add(new LabeledPoint(
                    3 + rnd.nextGaussian(),
                    4 + rnd.nextGaussian(),
                    "C"
            ));
        }

        // создаём классификатор
        KNNClassifier knn = new KNNClassifier(points, 5);

        // пример: классифицируем новую точку
        double px = 3.5;
        double py = 2.0;
        String predicted = knn.predict(px, py);
        System.out.printf("Point (%.2f, %.2f) -> class %s%n", px, py, predicted);

        // строим график и сохраняем в png
        savePlot(points, px, py, predicted, "knn_plot.png");
    }

    private static void savePlot(List<LabeledPoint> points,
                                 double px, double py,
                                 String pClass,
                                 String fileName) throws IOException {

        Map<String, XYSeries> seriesMap = new HashMap<>();

        for (LabeledPoint p : points) {
            seriesMap
                    .computeIfAbsent(p.label, XYSeries::new)
                    .add(p.x, p.y);
        }

        // серия для новой точки
        XYSeries newPoint = new XYSeries("new: " + pClass);
        newPoint.add(px, py);

        XYSeriesCollection dataset = new XYSeriesCollection();
        for (XYSeries s : seriesMap.values()) {
            dataset.addSeries(s);
        }
        dataset.addSeries(newPoint);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "KNN demo",
                "X", "Y",
                dataset
        );

        ChartUtils.saveChartAsPNG(new File(fileName), chart, 800, 600);
        System.out.println("Plot saved to " + fileName);
    }
}
