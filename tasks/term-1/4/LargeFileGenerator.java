import java.io.*;
import java.nio.file.*;

public class LargeFileGenerator {
    public static void main(String[] args) throws IOException {
        Path largeFile = Paths.get("huge_text.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(largeFile)) {
            for (int i = 0; i < 10_000_000; i++) { // 10 миллионов строк
                writer.write("This is line number " + i + " of a very large text file. ");
                writer.write("It contains multiple sentences to increase file size. ");
                writer.write("Hello my favorite world from java ");
                writer.newLine();
            }
        }
        System.out.println("File size: " + Files.size(largeFile) + " bytes");
    }
}