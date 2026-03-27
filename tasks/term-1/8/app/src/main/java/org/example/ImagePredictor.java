package org.example;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.util.Arrays;

public class ImagePredictor {

    public static void predictFromImage(MultiLayerNetwork model, String imagePath) throws Exception {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            System.out.println(imagePath + " not found – skipping prediction.");
            return;
        }

        NativeImageLoader loader = new NativeImageLoader(28, 28, 1);
        INDArray image = loader.asMatrix(imageFile);
        image.divi(255.0);

        INDArray output = model.output(image);
        int predicted = Nd4j.argMax(output, 1).getInt(0);

        System.out.println("Predicted digit: " + predicted);
        System.out.println("Raw output: " + Arrays.toString(output.toDoubleVector()));
    }
}