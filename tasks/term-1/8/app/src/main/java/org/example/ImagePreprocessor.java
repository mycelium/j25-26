package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;

import org.datavec.image.loader.NativeImageLoader;

import java.io.File;
import java.io.IOException;

public class ImagePreprocessor {

    private static final int HEIGHT = 28;
    private static final int WIDTH = 28;
    private static final int CHANNELS = 1;

    private final NativeImageLoader loader = new NativeImageLoader(HEIGHT, WIDTH, CHANNELS);

    public INDArray preprocess(String imagePath) throws IOException {
        try (INDArray image = loader.asMatrix(new File(imagePath))) {
            image.divi(255.0);
            return image.reshape(1, CHANNELS, HEIGHT, WIDTH);
        }
    }

}