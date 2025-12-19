package org.example;

import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class App {
    
    static final String filePathModel        = "src\\main\\resources\\model.zip";
    static final String filePathImageFolder  = "src\\main\\resources\\images";

    static ClassifierMLN cmln;

    public static void main(String[] args) {

        try
        {
            try {
                System.out.println("Trying to load model...");
                cmln = new ClassifierMLN(ModelSerializer.restoreMultiLayerNetwork(new File(filePathModel)));
                System.out.println("The model was successfully loaded.");
            } catch (IOException e) {
                System.out.println("Couldn't load the model.\nTraining process starts.");
                cmln = new ClassifierMLN();
                cmln.init();
                cmln.train(64, 10);
                cmln.saveModel(filePathModel);
                System.out.println("The model was successfully saved.");
            }
            System.out.println(ClassifierMLN.evalAnalysis(cmln.evaluate(64)));

            analyzePredictions(getPhotosFiles());
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void analyzePredictions(List<List<File>> testPhotos) throws IOException {
        int rightNumSum = 0;
        int wrongNumSum = 0;

        System.out.println("Test on hand-written photos:");
        for(int numb = 0; numb < testPhotos.size(); numb++)
        {
            int rightNum = 0;
            int wrongNum = 0;

            System.out.printf("Number %d\n----------\nPredictions: ",numb);

            for (File f : testPhotos.get(numb)) {
                int prNumb = cmln.predict(f);

                System.out.printf(prNumb + " ");

                if (numb == prNumb) rightNum++;
                else                wrongNum++;
            }
            System.out.printf("\nSummary:\n%d right answers\n%d wrong answers\n\n",
                              rightNum,wrongNum);
            rightNumSum += rightNum;
            wrongNumSum += wrongNum;
        }
        System.out.printf("Final Summary:\n%d right answers\n%d wrong answers\n",
                          rightNumSum,wrongNumSum);
    }

    public static List<List<File>> getPhotosFiles() {
        List<List<File>> res = new ArrayList<>(10);
        String basePath = filePathImageFolder;

        for (int i = 0; i < 10; i++) {
            res.add(new ArrayList<>());
        }

        for (int folderI = 0; folderI < 10; folderI++) {
            String folderPath = basePath + '\\' + folderI;
            File folder = new File(folderPath);

            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();

                if (files != null) {
                    for (File f : files) {
                        res.get(folderI).add(f);
                    }
                }
            } else System.err.println("Folder isn't found: " + folderPath);
        }
        return res;
    }
}
