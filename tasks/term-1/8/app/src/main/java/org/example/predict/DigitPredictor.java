package org.example.predict;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.example.config.ModelConfig;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Предсказание цифры на изображении.
 */
public class DigitPredictor {

    /**
     * Распознаёт цифру на изображении.
     *
     * @param model обученная модель
     * @param path путь к изображению
     * @return предсказанная цифра
     */
    public static int predict(MultiLayerNetwork model, String path) throws Exception {

        File file = new File(path);

        if (!file.exists()) 
            throw new FileNotFoundException("Image file not found: " + file.getAbsolutePath());

        BufferedImage original = ImageIO.read(file);

        if (original == null) 
            throw new IllegalArgumentException("Cannot read image: " + file.getAbsolutePath());
        
        BufferedImage gray = new BufferedImage( ModelConfig.WIDTH, ModelConfig.HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g = gray.createGraphics();
        g.drawImage(original, 0, 0, ModelConfig.WIDTH, ModelConfig.HEIGHT, null);
        g.dispose();

        double[] pixels = new double[ModelConfig.WIDTH * ModelConfig.HEIGHT];

        for (int y = 0; y < ModelConfig.HEIGHT; y++) 
            for (int x = 0; x < ModelConfig.WIDTH; x++) {
                int rgb = gray.getRGB(x, y);
                int value = rgb & 0xFF; 
                pixels[y * ModelConfig.WIDTH + x] = value / 255.0;
            }

        INDArray image = Nd4j.create(pixels, new long[]{1, ModelConfig.WIDTH * ModelConfig.HEIGHT});

        INDArray output = model.output(image);

        System.out.println("Output: " + output);

        return output.argMax(1).getInt(0);
    }
}