package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class MultipartPart {

    private final String name;
    private final String filename;
    private final String contentType;
    private final byte[] data;

    MultipartPart(String name, String filename, String contentType, byte[] data) {
        this.name = name;
        this.filename = filename;
        this.contentType = contentType;
        this.data = data != null ? Arrays.copyOf(data, data.length) : new byte[0];
    }

    public String getName() { return name; }
    public String getFilename() { return filename; }
    public String getContentType() { return contentType; }

    public byte[] getData() { return Arrays.copyOf(data, data.length); }

    public String getDataAsString() { return new String(data, StandardCharsets.UTF_8); }

    public boolean isFile() { return filename != null && !filename.isEmpty(); }
}
