package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageReader {

    private static final int IMAGE_WIDTH = 28;
    private static final int IMAGE_HEIGHT = 28;

    public static INDArray loadAndProcessImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            throw new IOException("Image not found: " + imagePath);
        }

        BufferedImage inputImage = ImageIO.read(imageFile);
        if (inputImage == null) {
            throw new IOException("Unsupported image format: " + imagePath);
        }

        BufferedImage grayImage = new BufferedImage(
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                BufferedImage.TYPE_BYTE_GRAY
        );

        Graphics2D g = grayImage.createGraphics();
        g.drawImage(inputImage, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
        g.dispose();

        INDArray input = Nd4j.create(1, 1, IMAGE_HEIGHT, IMAGE_WIDTH);

        for (int y = 0; y < IMAGE_HEIGHT; y++) {
            for (int x = 0; x < IMAGE_WIDTH; x++) {
                int pixel = grayImage.getRGB(x, y) & 0xFF;

                double value = pixel / 255.0;

                input.putScalar(new int[]{0, 0, y, x}, value);
            }
        }

        return input;
    }
}
