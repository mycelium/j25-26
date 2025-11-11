package org.example.neuralnet;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;

public class NeuralNet {

    private int batch = 64;
    private int seed = 123;
    private int epoches = 1;

    private MultiLayerNetwork model;

    public NeuralNet(){

        try {
                model = ModelSerializer.restoreMultiLayerNetwork(new File("mnist-model.zip"));
                return;
        } catch (IOException e) {
                System.out.println("Нет сохраненной модели");
        }

        DataSetIterator train;
        try {
            train = new MnistDataSetIterator(batch, true, seed);
        } catch (IOException e) {
            System.err.println("Ошибка при инициализации датасета");
            throw new RuntimeException();
        }

        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Adam(0.001))
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .nIn(1)
                        .stride(1, 1)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new ConvolutionLayer.Builder(5, 5)
                        .stride(1, 1)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nOut(256)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(10)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(org.deeplearning4j.nn.conf.inputs.InputType.convolutionalFlat(28, 28, 1))
                .build(); 
        
        model = new MultiLayerNetwork(config);
        model.init();
        model.setListeners(new ScoreIterationListener(10));
        
        System.out.println("Тренировка модели...");
        for (int i = 0; i < epoches; i++) {
            model.fit(train);
        }

        try {
                ModelSerializer.writeModel(model, new File("mnist-model.zip"), true);
        } catch (IOException e) {
                System.err.println("Ошибка при сохранении модели");
        }
    }

    public static INDArray prepareImageForMnist(String path) throws IOException {
        Mat src = opencv_imgcodecs.imread(path, opencv_imgcodecs.IMREAD_GRAYSCALE);
        if (src.empty()) {
                throw new IllegalArgumentException("Cannot load image: " + path);
        }

        Mat gray = new Mat();
        opencv_imgproc.equalizeHist(src, gray);

        Mat binary = new Mat();
        opencv_imgproc.threshold(
                gray,
                binary,
                0,
                255,
                opencv_imgproc.THRESH_BINARY | opencv_imgproc.THRESH_OTSU
                );
        opencv_core.bitwise_not(binary, binary);

        Mat kernel = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(2, 2));
        opencv_imgproc.dilate(binary, binary, kernel);

        MatVector contours = new MatVector();
        Mat hierarchy = new Mat();
        opencv_imgproc.findContours(
                binary.clone(),
                contours,
                hierarchy,
                opencv_imgproc.RETR_EXTERNAL,
                opencv_imgproc.CHAIN_APPROX_SIMPLE
        );

        if (contours.size() == 0) {
                throw new RuntimeException("No contours found (image may be too faint)");
        }

        Rect bbox = opencv_imgproc.boundingRect(contours.get(0));
        Mat digit = new Mat(binary, bbox);

        Mat resized20 = new Mat();
        opencv_imgproc.resize(digit, resized20, new Size(20, 20));

        Mat canvas = Mat.zeros(28, 28, opencv_core.CV_8UC1).asMat();
        int x = (28 - 20) / 2;
        int y = (28 - 20) / 2;
        Mat roi = new Mat(canvas, new Rect(x, y, 20, 20));
        resized20.copyTo(roi);

        opencv_imgcodecs.imwrite("../debug.png", canvas);

        INDArray array = Nd4j.create(1, 28, 28);
        for (int i = 0; i < 28; i++) {
                for (int j = 0; j < 28; j++) {
                double pixel = canvas.ptr(i, j).get() & 0xFF;
                array.putScalar(new int[]{0, i, j}, pixel / 255.0);
                }
        }

        return array.reshape(1, 1, 28, 28);
    }

    private int EvaluteImage(INDArray image){
        
        INDArray output = model.output(image);

        return output.argMax(1).getInt(0);
    }

    public Integer EvaluateImageFromPath(String imagePath){
        
        INDArray image;
        try {
                image = prepareImageForMnist(imagePath);
        } catch (IOException e) {
                System.err.printf("Ошибка чтения файла: %s\n", imagePath);
                return null;
        }

        return EvaluteImage(image);
    }

    public void GetStatistic(){
        
        DataSetIterator test;
        try {
            test = new MnistDataSetIterator(batch, true, seed);
        } catch (IOException e) {
            System.err.println("Ошибка при инициализации датасета. Невозможно собрать статистику.");
            return;
        }

        System.out.println("Вычисление точности...");
        System.out.println(model.evaluate(test).stats());
    }
}
