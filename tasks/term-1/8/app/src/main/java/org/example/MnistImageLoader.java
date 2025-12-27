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
        BufferedImage scaled = scaleTo28x28(grayscale);
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