package org.example;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.api.ndarray.INDArray;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ClassifierMLN {

        private MultiLayerNetwork model;
        private DataNormalization scaler = new ImagePreProcessingScaler(0, 1);

        public ClassifierMLN(){
            MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                    .seed(123)
                    .updater(new Adam(0.001))
                    .list()
                    .layer(new ConvolutionLayer.Builder(3, 3)
                            .nIn(1)
                            .stride(1, 1)
                            .nOut(32)
                            .activation(Activation.RELU)
                            .build())
                    .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                            .kernelSize(2, 2)
                            .stride(2, 2)
                            .build())
                    .layer(new ConvolutionLayer.Builder(3, 3)
                            .stride(1, 1)
                            .nOut(64)
                            .activation(Activation.RELU)
                            .build())
                    .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                            .kernelSize(2, 2)
                            .stride(2, 2)
                            .build())
                    .layer(new DenseLayer.Builder()
                            .nOut(128)
                            .activation(Activation.RELU)
                            .build())
                    .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .nOut(10)
                            .activation(Activation.SOFTMAX)
                            .build())
                    .setInputType(InputType.convolutionalFlat(28, 28, 1))
                    .build();

            model = new MultiLayerNetwork(config);
        }

        public ClassifierMLN(MultiLayerNetwork mtl){
            model = mtl;
        }

        public void init(){
            model.init();
        }

        public void train(int batchSize, int epochsNum) throws IOException {
            DataSetIterator trainIter = new MnistDataSetIterator(batchSize, true, 123);
            trainIter.setPreProcessor(scaler);

            System.out.println("Training:");
            for (int epochI = 0; epochI < epochsNum; epochI++) {
                System.out.printf("Epoch id - %d is in process.\n", epochI);
                model.fit(trainIter);
                trainIter.reset();
            }
        }

        public Evaluation evaluate(int batchSize) throws IOException {
            DataSetIterator testIter = new MnistDataSetIterator(batchSize, false, 123);
            testIter.setPreProcessor(scaler);
            return model.evaluate(testIter);
        }

        public static String evalAnalysis(Evaluation eval) {
            return String.format("""
                    Evaluation Analysis
                    -------------------
                    accuracy  | %.3f%%
                    precision | %.3f%%
                    recall    | %.3f%%
                    f1        | %.3f%%
                    ------------------
                    Confusion:
                    %s
                    """, eval.accuracy() * 100,    eval.precision() * 100,
                         eval.recall() * 100,      eval.f1() * 100,
                         eval.confusionToString());
        }

        public File saveModel(String filePath) throws IOException {
            File f = new File(filePath);
            ModelSerializer.writeModel(model, f, true);
            return f;
        }

        public int predict(File f) throws IOException {
            BufferedImage  image = ImageIO.read(f);
            INDArray       input = imageToINDArray(image);
            scaler.transform(input);

            return org.nd4j.linalg.factory.Nd4j.argMax(model.output(input), 1).getInt(0);
        }

        private INDArray imageToINDArray(BufferedImage image) {
        int width = 28;
        int height = 28;

        if (image.getWidth() != width || image.getHeight() != height) {
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            resized.getGraphics().drawImage(image, 0, 0, width, height, null);
            image = resized;
        }

        INDArray array = Nd4j.create(1, 1, height, width);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                array.putScalar(0, 0, y, x, gray / 255.0);
            }
        }
        return array;
    }
}
