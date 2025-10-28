import java.io.IOException;
import java.io.Reader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordFrequencyCounter {
    public enum ModeType {
        ALL_AT_ONCE,
        STREAM
    }

    private static final Pattern WORD_PATTERN = Pattern.compile(
            "\\b\\w+\\b",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    private static final long SMALL_FILE_THRESHOLD_BYTES = 2 * 1024 * 1024;

    private static final int DEFAULT_TOP_N = 100;

    private ModeType mode;

    private Map<String, Integer> countWordsAllAtOnce(Path filePath) throws IOException {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        return WORD_PATTERN.matcher(content.toLowerCase())
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toMap(
                        word -> word,
                        word -> 1,
                        Integer::sum,
                        HashMap::new
                ));
    }

    private Map<String, Integer> countWordsStreaming(Path filePath) throws IOException {
        Map<String, Integer> wordCounts = new HashMap<>();
        char[] buffer = new char[8192];
        StringBuilder carry = new StringBuilder();

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                String newChunk = new String(buffer, 0, bytesRead).toLowerCase();
                carry.append(newChunk);
                processBuffer(carry, wordCounts);
            }

            if (!carry.isEmpty()) {
                processBuffer(carry, wordCounts);
            }
        }
        return wordCounts;
    }

    private void processBuffer(StringBuilder carry, Map<String, Integer> wordCounts) {
        String text = carry.toString();
        Matcher matcher = WORD_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            String word = matcher.group();
            wordCounts.merge(word, 1, Integer::sum);
            lastEnd = matcher.end();
        }

        if (lastEnd < carry.length()) {
            carry.delete(0, lastEnd);
        }
        else {
            carry.setLength(0);
        }
    }

    private static ModeType getFileReadMode(Path filePath, String allAtOnceOut, String streamOut) {
        try {
            long size = Files.size(filePath);
            if (size <= SMALL_FILE_THRESHOLD_BYTES) {
                System.out.println(allAtOnceOut);
                return ModeType.ALL_AT_ONCE;
            }
            else {
                System.out.println(streamOut);
                return ModeType.STREAM;
            }
        }
        catch (IOException e) {
            System.err.println("[WARN] Could not read file size. " +
                    "Using [stream] mode to be safe.");
            return ModeType.STREAM;
        }
    }

    public WordFrequencyCounter() {
        this(ModeType.STREAM);
    }

    public WordFrequencyCounter(ModeType mode) {
        this.mode = mode;
    }

    public void printFrequencies(Map<String, Integer> frequencies) {
        if (frequencies.isEmpty()) {
            System.out.println("[NOTE] No words found.");
            return;
        }

        List<Map.Entry<String, Integer>> top = frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(WordFrequencyCounter.DEFAULT_TOP_N)
                .toList();

        int frSize = frequencies.size();
        boolean isNotShowingAll = DEFAULT_TOP_N < frSize;

        if (isNotShowingAll) {
            System.out.println("\nTop " + DEFAULT_TOP_N + " words:");
        }

        for (Map.Entry<String, Integer> entry : top) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        if (isNotShowingAll) {
            System.out.println("... (showing top " + DEFAULT_TOP_N + " of " + frSize + " unique words)");
        }
    }

    public Map<String, Integer> countWords(Path filePath) {
        Map<String, Integer> result = new HashMap<>();
        try {
            if (mode == ModeType.STREAM) {
                result = countWordsStreaming(filePath);
            }
            else {
                result = countWordsAllAtOnce(filePath);
            }
            return result;
        }
        catch (IOException e) {
            System.err.println("[ERROR] Can not read given file (" + filePath + ").");
            return result;
        }
    }

    public ModeType getMode() {
        return mode;
    }

    public void setMode(ModeType mode) {
        this.mode = mode;
    }

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Testing [all] mode on file Small Test.txt in directory");
            WordFrequencyCounter counterSmall = new WordFrequencyCounter(ModeType.ALL_AT_ONCE);
            Path filePath = Path.of(".\\tasks\\term-1\\4\\Small Test.txt");
            Map<String, Integer> frequencies = counterSmall.countWords(filePath);
            counterSmall.printFrequencies(frequencies);

            System.out.println("\n\n\n");

            System.out.println("Testing [stream] mode on file War & Peace.txt in directory");
            WordFrequencyCounter counterBig = new WordFrequencyCounter(ModeType.ALL_AT_ONCE);
            Path warAndPeaceFilePath = Path.of(".\\tasks\\term-1\\4\\War & Peace.txt");
            Map<String, Integer> warAndPeaceFrequencies = counterBig.countWords(warAndPeaceFilePath);
            counterSmall.printFrequencies(warAndPeaceFrequencies);

            return;
        }

        Path filePath = Path.of(args[0]);
        if (!Files.exists(filePath)) {
            System.err.println("[ERROR] File not found: " + filePath);
            System.exit(1);
        }

        ModeType selectedMode;
        if (args.length == 2) {
            String mode = args[1];
            String allAtOnceOut;
            String streamOut;

            if ("--stream".equals(mode)) {
                System.out.println("[OK] Using [stream] mode.");
                selectedMode = ModeType.STREAM;
            }
            else {
                if ("--all".equals(mode)) {
                    allAtOnceOut = "[OK] Using [all] mode.";
                    streamOut = "[WARN] File too large for [all] mode. Using [stream] mode instead.";
                }
                else {
                    System.err.println("[WARN] There is no mode called [" + mode + "].");
                    allAtOnceOut = "[NOTE] Using [all] mode due to file size.";
                    streamOut = "[NOTE] Using [stream] mode.";
                }

                selectedMode = getFileReadMode(filePath, allAtOnceOut, streamOut);
            }
        }
        else {
            selectedMode = getFileReadMode(
                    filePath,
                    "[NOTE] Using [all] mode due to file size.",
                    "[NOTE] Using [stream] mode."
            );
        }

        WordFrequencyCounter counter = new WordFrequencyCounter(selectedMode);
        Map<String, Integer> frequencies = counter.countWords(filePath);
        counter.printFrequencies(frequencies);
    }
}