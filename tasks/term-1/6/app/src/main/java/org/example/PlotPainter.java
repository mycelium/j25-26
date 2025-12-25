package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Map;

public class PlotPainter
{
    private static final int GRAPH_WIDTH = 800;
    private static final int GRAPH_HEIGHT = 800;
    private static final int LEGEND_WIDTH = 200;
    private static final int TOTAL_WIDTH = GRAPH_WIDTH + LEGEND_WIDTH;
    private static final int TOTAL_HEIGHT = GRAPH_HEIGHT + 100;
    private static final int GRAPH_MARGIN = 50;

    private static final Map<String, Color> COLORS = Map.of(
            "upperLeft", Color.ORANGE,
            "upperRight", Color.GREEN,
            "lowerLeft", Color.BLUE,
            "lowerRight", Color.MAGENTA
    );

    private static final int POINT_SIZE = 8;
    private static final int NEW_POINT_SIZE = 12;

    public static void createPlot(List<Point> trainingData, Point newPoint, String fileName)
    {
        try
        {
            BufferedImage image = new BufferedImage(TOTAL_WIDTH, TOTAL_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, TOTAL_WIDTH, TOTAL_HEIGHT);

            int graphAreaX = 0;
            int graphAreaY = 0;

            int legendAreaX = GRAPH_WIDTH;
            int legendAreaY = 0;

            drawGraph(g, graphAreaX, graphAreaY, GRAPH_WIDTH, GRAPH_HEIGHT, trainingData, newPoint);
            drawLegend(g, legendAreaX, legendAreaY, LEGEND_WIDTH, TOTAL_HEIGHT);

            ImageIO.write(image, "png", new File(fileName));
            g.dispose();

        }
        catch (IOException e)
        {
            System.err.println("Error saving the graph: " + e.getMessage());
        }
    }

    private static void drawGraph(Graphics2D g, int x, int y, int width, int height,
                                  List<Point> trainingData, Point newPoint)
    {
        double minX = 0, maxX = 10;
        double minY = 0, maxY = 10;

        // Calculating the drawing area with margins
        int plotX = x + GRAPH_MARGIN;
        int plotY = y + GRAPH_MARGIN;
        int plotWidth = width - 2 * GRAPH_MARGIN;
        int plotHeight = height - 2 * GRAPH_MARGIN;

        g.setColor(Color.BLACK);
        int xAxisY = plotY + plotHeight;
        g.drawLine(plotX, xAxisY, plotX + plotWidth, xAxisY);
        g.drawLine(plotX, plotY, plotX, plotY + plotHeight);

        // creating divisions on the axes
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i <= 10; i++)
        {
            int tickX = plotX + (int)(i * plotWidth / 10.0);
            int tickY = plotY + (int)((10 - i) * plotHeight / 10.0);

            g.drawLine(tickX, xAxisY - 5, tickX, xAxisY + 5);
            String xLabel = String.valueOf(i);
            int labelWidth = g.getFontMetrics().stringWidth(xLabel);
            g.drawString(xLabel, tickX - labelWidth/2, xAxisY + 20);

            g.drawLine(plotX - 5, tickY, plotX + 5, tickY);
            g.drawString(String.valueOf(i), plotX - 25, tickY + 5);
        }

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("X", plotX + plotWidth + 10, xAxisY);
        g.drawString("Y", plotX, plotY - 10);

        for (Point point : trainingData)
        {
            drawPointOnGraph(g, point, false, plotX, plotY, plotWidth, plotHeight);
        }

        if (newPoint != null)
        {
            drawPointOnGraph(g, newPoint, true, plotX, plotY, plotWidth, plotHeight);
        }
    }

    private static void drawPointOnGraph(Graphics2D g, Point point, boolean isNew,
                                         int plotX, int plotY, int plotWidth, int plotHeight)
    {
        int screenX = plotX + (int)(point.getX() * plotWidth / 10.0);
        int screenY = plotY + (int)((10 - point.getY()) * plotHeight / 10.0);

        Color color = COLORS.get(point.getPointClass());

        if (color == null)
        {
            color = Color.GRAY;
        }

        if (isNew)
        {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(2));
            g.drawOval(screenX - NEW_POINT_SIZE/2, screenY - NEW_POINT_SIZE/2, NEW_POINT_SIZE, NEW_POINT_SIZE);
            g.setStroke(new BasicStroke(1));

            g.setColor(color);
            g.fillOval(screenX - NEW_POINT_SIZE/2 + 2, screenY - NEW_POINT_SIZE/2 + 2,NEW_POINT_SIZE - 4, NEW_POINT_SIZE - 4);
        }
        else
        {
            g.setColor(color);
            g.fillOval(screenX - POINT_SIZE/2, screenY - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
        }
    }

    private static void drawLegend(Graphics2D g, int x, int y, int width, int height)
    {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Class colors", x + 20, y + 40);

        int currentY = y + 80;
        int lineHeight = 30;

        g.setFont(new Font("Arial", Font.PLAIN, 14));

        drawLegendItem(g, x + 20, currentY, Color.ORANGE, "upperLeft");
        currentY += lineHeight;

        drawLegendItem(g, x + 20, currentY, Color.GREEN, "upperRight");
        currentY += lineHeight;

        drawLegendItem(g, x + 20, currentY, Color.BLUE, "lowerLeft");
        currentY += lineHeight;

        drawLegendItem(g, x + 20, currentY, Color.MAGENTA, "lowerRight");
        currentY += lineHeight;

        currentY += 10;
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(2));
        g.drawRect(x + 20, currentY - 10, 20, 20);
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);
        g.drawString("New point", x + 50, currentY + 5);
    }

    private static void drawLegendItem(Graphics2D g, int x, int y, Color color, String text)
    {
        g.setColor(color);
        g.fillRect(x, y - 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRect(x, y - 10, 20, 20);
        g.drawString(text, x + 30, y + 5);
    }
}