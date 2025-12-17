package org.MNIST_CNN;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageProcessor {
    
    public static double[] loadAndProcessImage(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        if (image == null) {
            throw new IOException("Could not load image: " + imagePath);
        }
        BufferedImage grayImage = convertToGrayscale(image);
        BufferedImage resizedImage = resizeImage(grayImage, 28, 28);
        return imageToArray(resizedImage);
    }
    
    private static BufferedImage convertToGrayscale(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return image;
        }
        
        BufferedImage grayImage = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return grayImage;
    }
    
    private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return resizedImage;
    }
    
    private static double[] imageToArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[] array = new double[width * height];
        
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                array[index++] = (255 - pixel) / 255.0;
            }
        }
        
        return array;
    }
}