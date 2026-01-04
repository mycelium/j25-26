package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MnistImageLoader {

    public static double[] loadAndProcessImage(String path) throws IOException {
        BufferedImage original = ImageIO.read(new File(path));
        if (original == null) {
            throw new IOException("Image file not readable: " + path);
        }

        BufferedImage grayscale = toGrayscale(original);
        BufferedImage scaled = centerDigit(scaleTo28x28(grayscale));
        return pixelsToNormalizedArray(scaled);
    }

    private static BufferedImage toGrayscale(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_BYTE_GRAY)
            return src;

        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = gray.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return gray;
    }

    private static BufferedImage scaleTo28x28(BufferedImage image) {
        BufferedImage output = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = output.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(image, 0, 0, 28, 28, null);
        g.dispose();
        return output;
    }

    private static BufferedImage centerDigit(BufferedImage img) {
        int minX = 28, maxX = 0, minY = 28, maxY = 0;
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int gray = 255 - (img.getRGB(x, y) & 0xFF); // invert back to "ink"
                if (gray > 10) { // порог шума
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (minX > maxX) return img; // пустое изображение


        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        BufferedImage cropped = img.getSubimage(minX, minY, width, height);


        BufferedImage centered = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = centered.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 28, 28);
        int dx = (28 - width) / 2;
        int dy = (28 - height) / 2;
        g.drawImage(cropped, dx, dy, null);
        g.dispose();
        return centered;
    }

    private static double[] pixelsToNormalizedArray(BufferedImage img) {
        double[] data = new double[28 * 28];
        int idx = 0;
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int rgb = img.getRGB(x, y);
                int grayValue = rgb & 0xFF;
                data[idx++] = (255.0 - grayValue) / 255.0; // invert + normalize
            }
        }
        return data;
    }
}
