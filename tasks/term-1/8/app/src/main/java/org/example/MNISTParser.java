package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MNISTParser {

    public static final int IMAGE_MAGIC = 0x00000803;
    public static final int LABEL_MAGIC = 0x00000801;

    public static INDArray loadImages(String filePath) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {
            int magic = dis.readInt();
            if (magic != IMAGE_MAGIC) {
                throw new IllegalArgumentException("Not an MNIST image file : " + filePath);
            }

            int numImages = dis.readInt();
            int rows = dis.readInt();
            int cols = dis.readInt();

            byte[] data = new byte[numImages * rows * cols];
            dis.readFully(data);

            double[] flat = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                flat[i] = (data[i] & 0xFF) / 255.0;
            }

            return Nd4j.create(flat, new int[]{numImages, 1, rows, cols});
        }
    }

    public static INDArray loadLabels(String filePath) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {
            int magic = dis.readInt();
            if (magic != LABEL_MAGIC) {
                throw new IllegalArgumentException("Not an MNIST label file: " + filePath);
            }

            int numLabels = dis.readInt();
            byte[] labels = new byte[numLabels];
            dis.readFully(labels);

            double[][] oneHot = new double[numLabels][10];
            for (int i = 0; i < numLabels; i++) {
                int label = labels[i] & 0xFF;
                oneHot[i][label] = 1.0;
            }

            return Nd4j.create(oneHot);
        }
    }

}