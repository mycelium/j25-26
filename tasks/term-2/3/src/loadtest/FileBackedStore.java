package loadtest;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class FileBackedStore {
    private final Path path;

    FileBackedStore(Path path) throws IOException {
        this.path = path;
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    synchronized String appendAndReadLast(String jsonLine) throws IOException {
        Files.writeString(
                path,
                jsonLine + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
        return readLastLine();
    }

    private String readLastLine() throws IOException {
        if (!Files.exists(path) || Files.size(path) == 0L) {
            return "";
        }

        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
            long pointer = file.length() - 1L;
            StringBuilder reversed = new StringBuilder();
            boolean seenContent = false;

            while (pointer >= 0L) {
                file.seek(pointer);
                int value = file.read();
                if (value == '\n' || value == '\r') {
                    if (seenContent) {
                        break;
                    }
                } else {
                    seenContent = true;
                    reversed.append((char) value);
                }
                pointer--;
            }

            return reversed.reverse().toString();
        }
    }
}
