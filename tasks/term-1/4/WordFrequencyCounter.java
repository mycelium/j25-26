import java.util.*;
import java.nio.file.*;
import java.io.*;

public class WordFrequencyCounter {

	private static final long LIMIT_SIZE = 1024 * 100; //100 KB

	public static Map<String, Integer> countWords(Path filePath) {
		// read file, tokenize words, update map
		Map<String, Integer> wordCount = new HashMap<>();

		try {
			long fileSize = Files.size(filePath);
			if (fileSize < LIMIT_SIZE) {
				//entire file into memory
				String text = Files.readString(filePath);
				processText(text, wordCount);
			} else {
				//stream file line byline
				try (BufferedReader reader = Files.newBufferedReader(filePath)) {
					String line;
					while ((line = reader.readLine()) != null) {
						processText(line, wordCount);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("File has not been processed: " + e.getMessage());
		}
		
		return wordCount;
	}

	private static void processText(String text, Map<String, Integer> wordCount) {
		String[] words = text.split("\\s+");
		for (String word : words) {
			word = word.toLowerCase().replaceAll("[^a-zа-я]", "");
			if (!word.isEmpty()) {
				wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
			}
		}
	}

	public static void printFrequencies(Map<String, Integer> frequencies) {
		// print word counts
		for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
			System.out.printf("%-30s : %d \n", entry.getKey(), entry.getValue());
		}
	}

	public static void main(String[] args) {
		// run word frequency counter
		if (args.length == 0) {
			System.out.println("Usage: java WordFrequencyCounter <file_path>");
			return;
		}	
		
		Path filePath = Path.of(args[0]);
		
		long startTime = System.currentTimeMillis();
		Map<String, Integer> result = countWords(filePath);
		long endTime = System.currentTimeMillis();

		printFrequencies(result);
		System.out.println("Processing time: " + (endTime - startTime) + " ms\n");
	}
}
