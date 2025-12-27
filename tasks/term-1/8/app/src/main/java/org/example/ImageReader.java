package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageReader {
    public static INDArray readImage(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedImage image = ImageIO.read(file);
        BufferedImage invertedImage = invertColors(image);
        BufferedImage resizedImage = resizeImage(invertedImage, 28, 28);
        return normalizeImage(resizedImage);
    }
    private static BufferedImage invertColors(BufferedImage image) {
        BufferedImage inverted = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int invertedRGB = 255 - (rgb & 0xFF);
                int newRGB = (invertedRGB << 16) | (invertedRGB << 8) | invertedRGB;
                inverted.setRGB(x, y, newRGB);
            }
        }
        return inverted;
    }
    private static BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();
        return resized;
    }
    private static INDArray normalizeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        INDArray array = Nd4j.create(1, 1, height, width);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int grayValue = rgb & 0xFF;
                float normalized = grayValue / 255.0f;
                array.putScalar(new int[]{0, 0, y, x}, normalized);
            }
        }
        return array;
    }
}
