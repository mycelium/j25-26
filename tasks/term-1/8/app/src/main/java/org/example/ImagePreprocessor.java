package org.example;

import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

import java.io.InputStream;

public class ImagePreprocessor {
    private final int targetHeight;
    private final int targetWidth;

    public ImagePreprocessor(int height, int width) {
        this.targetHeight = height;
        this.targetWidth = width;
    }

    public INDArray transformStream(InputStream dataStream) throws Exception {
        NativeImageLoader imgLoader = new NativeImageLoader(targetHeight, targetWidth, 1);
        INDArray matrix = imgLoader.asMatrix(dataStream);

        ImagePreProcessingScaler normalizer = new ImagePreProcessingScaler(0, 1);
        normalizer.transform(matrix);

        return matrix;
    }
}