package model.digitPredictionModel;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.datavec.image.loader.NativeImageLoader;
import java.io.File;
import java.io.IOException;

public class DigitPredictor {
    private final MultiLayerNetwork model;
    private final ImagePreProcessingScaler scaler;

    public DigitPredictor(MultiLayerNetwork model, ImagePreProcessingScaler scaler) {
        this.model = model;
        this.scaler = scaler;
    }

    public int predict(INDArray input) {
        INDArray output = model.output(input);
        return output.argMax(1).getInt(0);
    }

    public int predictFromImage(String imagePath) throws IOException {
        NativeImageLoader loader = new NativeImageLoader(28, 28, 1, false);
        INDArray image = loader.asMatrix(new File(imagePath));
        scaler.transform(image);
        return predict(image);
    }
}