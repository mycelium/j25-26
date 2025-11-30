package org.example;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MNISTLoader {
    private List<double[]> images;
    private List<Integer> labels;
    
    public MNISTLoader(String imagesPath, String labelsPath) throws Exception {
        images = new ArrayList<>();
        labels = new ArrayList<>();
        loadData(imagesPath, labelsPath);
    }
    
    private void loadData(String imagesPath, String labelsPath) throws Exception {
        DataInputStream imagesStream = new DataInputStream(new FileInputStream(imagesPath));
        int MNISTFormatNumberImages = imagesStream.readInt();
        int numImages = imagesStream.readInt();
        int rows = imagesStream.readInt();
        int cols = imagesStream.readInt();
        
        DataInputStream labelsStream = new DataInputStream(new FileInputStream(labelsPath));
        int MNISTFormatNumberLabels = labelsStream.readInt();
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
    
    public int getSize() {
        return images.size();
    }
    
    public double[] getImage(int index) {
        return images.get(index);
    }
    
    public int getLabel(int index) {
        return labels.get(index);
    }
    
    public List<double[]> getAllImages() {
        return new ArrayList<>(images);
    }
    
    public List<Integer> getAllLabels() {
        return new ArrayList<>(labels);
    }
}