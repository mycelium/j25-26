package org.example;

public class App
{
    public static void main(String[] args)
    {
        //args = new String[] {"five.png"};
        try
        {
            MnistDigitClassifier classifier = new MnistDigitClassifier();
            classifier.loadOrTrainModel();

            System.out.println("\n>>> Running Model Evaluation Statistics:");
            classifier.printStats();

            String inputFile;

            if (args.length > 0)
            {
                inputFile = args[0];
            } else
            {
                inputFile = "five.png";
            }

            int digit = classifier.predictDigit(inputFile);
            System.out.println("================================");
            System.out.println("Result for file " + inputFile + ": " + digit);
            System.out.println("================================");
        }
        catch (Exception e)
        {
            System.err.println("Errpr: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}