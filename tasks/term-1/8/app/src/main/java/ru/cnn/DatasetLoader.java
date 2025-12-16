package ru.cnn;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatasetLoader {
    private List<double[]> images;
    private List<Integer> labels;


    public int getSize() { return images.size(); }

    public double[] getImage(int index) { return images.get(index); }

    public int getLabel(int index) { return labels.get(index); }

    public List<double[]> getImages() { return new ArrayList<>(images); }

    public List<Integer> getLabels() { return new ArrayList<>(labels); }


    public DatasetLoader(String imagesPath, String labelsPath) throws IOException {
        images = new ArrayList<>();
        labels = new ArrayList<>();

        try (DataInputStream imagesStream = new DataInputStream(new FileInputStream(imagesPath));
             DataInputStream labelsStream = new DataInputStream(new FileInputStream(labelsPath))) {

            imagesStream.readInt(); // To read format (not needed)
            int numImages = imagesStream.readInt();
            int rows = imagesStream.readInt();
            int cols = imagesStream.readInt();

            labelsStream.readInt(); // To read format (not needed)
            int numLabels = labelsStream.readInt();

            for (int i = 0; i < Math.min(numImages, numLabels); i++) {
                byte[] bytes = new byte[rows * cols];
                imagesStream.readFully(bytes);

                double[] image = new double[rows * cols];
                for (int j = 0; j < bytes.length; j++) {
                    image[j] = (bytes[j] & 0xFF) / 255.0;
                }

                images.add(image);

                int label = labelsStream.readUnsignedByte();
                labels.add(label);
            }
        }
    }
}