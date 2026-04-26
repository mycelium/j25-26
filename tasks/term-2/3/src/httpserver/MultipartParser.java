package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartParser {
    
    public static List<FormPart> parse(byte[] data, String boundary) {
        List<FormPart> result = new ArrayList<>();
        byte[] delimiter = ("--" + boundary).getBytes(StandardCharsets.US_ASCII);
        byte[] finalDelim = ("--" + boundary + "--").getBytes(StandardCharsets.US_ASCII);
        int pos = 0;
        
        while (pos < data.length) {
            int partStart = indexOf(data, delimiter, pos);
            if (partStart == -1) break;
            if (indexOf(data, finalDelim, partStart) == partStart) break;
            partStart += delimiter.length;
            if (partStart + 1 < data.length && data[partStart] == '\r' && data[partStart + 1] == '\n') {
                partStart += 2;
            }
            int headerEnd = indexOf(data, "\r\n\r\n".getBytes(), partStart);
            if (headerEnd == -1) break;
            String headerBlock = new String(data, partStart, headerEnd - partStart, StandardCharsets.UTF_8);
            Map<String, String> partHeaders = new HashMap<>();
            for (String line : headerBlock.split("\r\n")) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    partHeaders.put(line.substring(0, colon).trim().toLowerCase(),
                                   line.substring(colon + 1).trim());
                }
            }
            int contentStart = headerEnd + 4;
            int nextBoundary = indexOf(data, delimiter, contentStart);
            if (nextBoundary == -1) break;
            int contentEnd = nextBoundary - 2;
            byte[] content = java.util.Arrays.copyOfRange(data, contentStart, contentEnd);
            result.add(new FormPart(partHeaders, content));
            pos = nextBoundary;
        }
        return result;
    }
    
    private static int indexOf(byte[] haystack, byte[] needle, int offset) {
        outer: for (int i = offset; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }
}
