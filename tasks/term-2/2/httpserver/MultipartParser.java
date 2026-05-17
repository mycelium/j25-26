package httpserver;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MultipartParser {

    private MultipartParser() {}

    static List<MultipartPart> parse(byte[] body, String boundary) {
        List<MultipartPart> result = new ArrayList<>();
        if (body == null || body.length == 0 || boundary == null) return result;

        byte[] delim = ("--" + boundary).getBytes(StandardCharsets.US_ASCII);
        byte[] endDelim = ("--" + boundary + "--").getBytes(StandardCharsets.US_ASCII);

        int pos = indexOf(body, delim, 0);
        if (pos == -1) return result;

        pos += delim.length;
        pos = skipCRLF(body, pos);

        while (pos < body.length) {
            int nextDelim = indexOf(body, delim,    pos);
            int nextEndDelim = indexOf(body, endDelim, pos);

            // this patr ends?
            boolean last;
            int endPos;
            if (nextEndDelim != -1 && (nextDelim == -1 || nextEndDelim <= nextDelim)) {
                endPos = nextEndDelim;
                last = true;
            } else if (nextDelim != -1) {
                endPos = nextDelim;
                last = false;
            } else {
                break;
            }

            int partEnd = endPos;
            if (partEnd >= 2 && body[partEnd - 2] == '\r' && body[partEnd - 1] == '\n') {
                partEnd -= 2;
            } else if (partEnd >= 1 && body[partEnd - 1] == '\n') {
                partEnd -= 1;
            }

            MultipartPart part = parsePart(Arrays.copyOfRange(body, pos, partEnd));
            if (part != null) result.add(part);

            pos = endPos + (last ? endDelim.length : delim.length);
            pos = skipCRLF(body, pos);
            if (last) break;
        }

        return result;
    }

    private static MultipartPart parsePart(byte[] data) {
        int sep = findDoubleCRLF(data);
        if (sep == -1) return null;

        // Header - ISO-8859-1; body starts after \r\n\r\n (4 bytes) or \n\n (2 bytes)
        boolean crlf = sep + 3 < data.length && data[sep] == '\r';
        int bodyStart = sep + (crlf ? 4 : 2);
        String headerStr = new String(data, 0, sep, StandardCharsets.ISO_8859_1);
        byte[] body = Arrays.copyOfRange(data, bodyStart, data.length);

        Map<String, String> headers = new HashMap<>();
        for (String line : headerStr.split("\\r?\\n")) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                headers.put(line.substring(0, colon).trim().toLowerCase(),
                            line.substring(colon + 1).trim());
            }
        }

        String disposition = headers.get("content-disposition");
        if (disposition == null || !disposition.contains("form-data")) return null;

        String name = extractQuoted(disposition, "name");
        String filename = extractQuoted(disposition, "filename");
        String contentType = headers.get("content-type");

        return new MultipartPart(name, filename, contentType, body);
    }

    private static int findDoubleCRLF(byte[] data) {
        for (int i = 0; i < data.length - 1; i++) {
            if (data[i] == '\n' && data[i + 1] == '\n') return i;
            if (i + 3 < data.length
                    && data[i] == '\r' && data[i+1] == '\n'
                    && data[i+2] == '\r' && data[i+3] == '\n') return i;
        }
        return -1;
    }

    private static int indexOf(byte[] haystack, byte[] needle, int from) {
        outer:
        for (int i = from; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    private static int skipCRLF(byte[] data, int pos) {
        if (pos < data.length && data[pos] == '\r') pos++;
        if (pos < data.length && data[pos] == '\n') pos++;
        return pos;
    }

    private static String extractQuoted(String src, String param) {
        String token = param + "=\"";
        int idx = src.indexOf(token);
        if (idx < 0) return null;
        int start = idx + token.length();
        int end = src.indexOf('"', start);
        return end > start ? src.substring(start, end) : null;
    }
}
