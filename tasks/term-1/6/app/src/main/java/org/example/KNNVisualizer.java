package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class KNNVisualizer {

    public void visualize(KNNClassifier classifier, Point unknownPoint) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries seriesX = new XYSeries("Class-X");
        XYSeries seriesY = new XYSeries("Class-Y");
        XYSeries seriesZ = new XYSeries("Class-Z");

        for (Point p : classifier.getTrainingData()) {
            if (p.getLabel().equals("Class-X")) {
                seriesX.add(p.getX(), p.getY());
            } else if (p.getLabel().equals("Class-Y")) {
                seriesY.add(p.getX(), p.getY());
            } else if (p.getLabel().equals("Class-Z")) {
                seriesZ.add(p.getX(), p.getY());
            }
        }

        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "KNN Classifier",
                "X",
                "Y",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        javax.swing.JOptionPane.showMessageDialog(null, chartPanel);
    }
}
