package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageReader {

    private static final int WIDTH = 28;
    private static final int HEIGHT = 28;
    private static final int CHANNELS = 1;

    public static INDArray loadAndProcessImage(String imagePath) throws IOException {

        BufferedImage sourceImage = readImage(imagePath);
        BufferedImage grayImage = convertToGrayAndResize(sourceImage);

        return convertToINDArray(grayImage);
    }


    private static BufferedImage readImage(String imagePath) throws IOException {
        File file = new File(imagePath);

        if (!file.exists()) {
            throw new IOException("Image not found: " + imagePath);
        }

        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Unsupported image format: " + imagePath);
        }

        return image;
    }


    private static BufferedImage convertToGrayAndResize(BufferedImage input) {
        BufferedImage grayImage =
                new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D graphics = grayImage.createGraphics();
        graphics.drawImage(input, 0, 0, WIDTH, HEIGHT, null);
        graphics.dispose();

        return grayImage;
    }


    private static INDArray convertToINDArray(BufferedImage image) {

        INDArray array = Nd4j.create(1, CHANNELS, HEIGHT, WIDTH);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                double normalized = pixel / 255.0;
                array.putScalar(new int[]{0, 0, y, x}, normalized);
            }
        }

        return array;
    }
}
