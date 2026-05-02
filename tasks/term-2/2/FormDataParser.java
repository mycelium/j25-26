import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDataParser {

    public static List<FormPart> parse(byte[] body, String boundary) {
        List<FormPart> parts = new ArrayList<>();
        
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] finalBoundaryBytes = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);

        int currentIndex = findIndex(body, boundaryBytes, 0);
        if (currentIndex == -1) return parts;

        while (true) {
            currentIndex += boundaryBytes.length;

            if (currentIndex + 1 < body.length && body[currentIndex] == '-' && body[currentIndex + 1] == '-') {
                break;
            }

            if (currentIndex + 1 < body.length && body[currentIndex] == '\r' && body[currentIndex + 1] == '\n') {
                currentIndex += 2;
            }

            Map<String, String> partHeaders = new HashMap<>();
            while (currentIndex < body.length) {
                int lineEndIndex = findIndex(body, new byte[]{'\r', '\n'}, currentIndex);
                
                if (lineEndIndex == -1 || lineEndIndex == currentIndex) {
                    currentIndex += 2; 
                    break;
                }
                
                String headerLine = new String(body, currentIndex, lineEndIndex - currentIndex, StandardCharsets.UTF_8);
                String[] headerParts = headerLine.split(": ", 2);
                if (headerParts.length == 2) {
                    partHeaders.put(headerParts[0].trim(), headerParts[1].trim());
                }
                currentIndex = lineEndIndex + 2;
            }

            int nextBoundaryIndex = findIndex(body, boundaryBytes, currentIndex);
            if (nextBoundaryIndex == -1) break;

            int contentEndIndex = nextBoundaryIndex - 2;
            
            if (contentEndIndex < currentIndex) {
                currentIndex = nextBoundaryIndex;
                continue;
            }

            byte[] partContent = new byte[contentEndIndex - currentIndex];
            System.arraycopy(body, currentIndex, partContent, 0, partContent.length);

            parts.add(new FormPart(partHeaders, partContent));

            currentIndex = nextBoundaryIndex;

            if (startsWith(body, finalBoundaryBytes, currentIndex)) {
                break;
            }
        }

        return parts;
    }

    private static int findIndex(byte[] haystack, byte[] needle, int startIndex) {
        for (int i = startIndex; i <= haystack.length - needle.length; i++) {
            boolean found = true;
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    private static boolean startsWith(byte[] array, byte[] prefix, int offset) {
        if (offset + prefix.length > array.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (array[offset + i] != prefix[i]) return false;
        }
        return true;
    }
}
