import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartParser {

    public static List<MultipartPart> parse(byte[] body, String boundary) {
        List<MultipartPart> parts = new ArrayList<>();

        byte[] delimiter = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] finalDelimiter = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);

        int pos = 0;

        pos = indexOf(body, delimiter, 0);
        if (pos == -1) return parts;

        while (true) {
            pos += delimiter.length;

            if (pos + 1 < body.length && body[pos] == '-' && body[pos + 1] == '-') break;

            if (pos + 1 < body.length && body[pos] == '\r' && body[pos + 1] == '\n') {
                pos += 2;
            }

            Map<String, String> partHeaders = new HashMap<>();
            while (pos < body.length) {
                int lineEnd = indexOf(body, new byte[]{'\r', '\n'}, pos);
                if (lineEnd == -1 || lineEnd == pos) {
                    pos += 2;
                    break;
                }
                String headerLine = new String(body, pos, lineEnd - pos, StandardCharsets.UTF_8);
                String[] kv = headerLine.split(": ", 2);
                if (kv.length == 2) {
                    partHeaders.put(kv[0].trim(), kv[1].trim());
                }
                pos = lineEnd + 2;
            }

            int nextDelimiter = indexOf(body, delimiter, pos);
            if (nextDelimiter == -1) break;

            int bodyEnd = nextDelimiter - 2;
            byte[] partBody = new byte[bodyEnd - pos];
            System.arraycopy(body, pos, partBody, 0, partBody.length);

            parts.add(new MultipartPart(partHeaders, partBody));

            pos = nextDelimiter;

            if (startsWith(body, finalDelimiter, pos)) break;
        }

        return parts;
    }

    private static int indexOf(byte[] haystack, byte[] needle, int fromIndex) {
        outer:
        for (int i = fromIndex; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    private static boolean startsWith(byte[] haystack, byte[] needle, int offset) {
        if (offset + needle.length > haystack.length) return false;
        for (int i = 0; i < needle.length; i++) {
            if (haystack[offset + i] != needle[i]) return false;
        }
        return true;
    }
}