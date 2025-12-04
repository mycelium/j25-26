package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MNISTLoader {

    private final List<double[]> images;
    private final List<Integer> labels;

    private static final String MNIST_DIRECTORY = "tasks\\term-1\\8\\app\\src\\main\\resources\\MNIST dataset\\";

    public MNISTLoader(String imageFile, String labelFile) throws Exception {
        images = new ArrayList<>();
        labels = new ArrayList<>();
        loadData(imageFile, labelFile);
    }

    private void loadData(String imageFile, String labelFile) throws Exception {
        String imagesPath = MNIST_DIRECTORY + imageFile;
        String labelsPath = MNIST_DIRECTORY + labelFile;

        DataInputStream imagesStream = new DataInputStream(new FileInputStream(imagesPath));
        int magicImagesNum = imagesStream.readInt();
        int numImages = imagesStream.readInt();
        int rows = imagesStream.readInt();
        int cols = imagesStream.readInt();

        DataInputStream labelsStream = new DataInputStream(new FileInputStream(labelsPath));
        int magicLabelsNum = labelsStream.readInt();
        int numLabels = labelsStream.readInt();

        System.out.println("Loading " + Math.min(numImages, numLabels) + " examples");

        for (int i = 0; i < Math.min(numImages, numLabels); i++) {
            byte[] imageBytes = new byte[rows * cols];
            imagesStream.readFully(imageBytes);

            double[] image = new double[rows * cols];
            for (int j = 0; j < imageBytes.length; j++) {
                image[j] = (imageBytes[j] & 0xFF) / 255.0;
            }
            images.add(image);

            int label = labelsStream.readByte() & 0xFF;
            labels.add(label);
        }

        imagesStream.close();
        labelsStream.close();
    }

    public List<double[]> getAllImages() {
        return new ArrayList<>(images);
    }

    public List<Integer> getAllLabels() {
        return new ArrayList<>(labels);
    }

}