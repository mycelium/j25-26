package org.example;

import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.IOException;

public class ImageProcessor {

    public INDArray loadAndPreprocessImage(String imagePath) throws IOException {
        NativeImageLoader loader = new NativeImageLoader(28, 28, 1);

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("[ERROR] File not found: " + imagePath);
        }

        try (INDArray image = loader.asMatrix(imageFile)) {
            image.divi(255.0);
            return image.reshape(1, 1, 28, 28);
        }
    }

}