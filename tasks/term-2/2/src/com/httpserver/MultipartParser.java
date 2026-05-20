package com.httpserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MultipartParser {

    public static final class Part {
        private final Map<String, String> headers;
        private final byte[] data;

        Part(Map<String, String> headers, byte[] data) {
            this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
            this.data    = data.clone();
        }

        
        public Optional<String> getHeader(String name) {
            return Optional.ofNullable(headers.get(name.toLowerCase()));
        }

        
        public Optional<String> getName() {
            return getHeader("content-disposition").flatMap(v -> extractParam(v, "name"));
        }

       
        public Optional<String> getFilename() {
            return getHeader("content-disposition").flatMap(v -> extractParam(v, "filename"));
        }

        
        public byte[] getData() { return data.clone(); }

        
        public String getDataAsString() {
            return new String(data, StandardCharsets.UTF_8);
        }

        private static Optional<String> extractParam(String header, String param) {
            String search = param + "=";
            int idx = header.indexOf(search);
            if (idx < 0) return Optional.empty();
            int start = idx + search.length();
            if (start >= header.length()) return Optional.empty();
            if (header.charAt(start) == '"') {
                int end = header.indexOf('"', start + 1);
                return end > start ? Optional.of(header.substring(start + 1, end)) : Optional.empty();
            }
            int end = header.indexOf(';', start);
            return Optional.of(end < 0 ? header.substring(start).trim() : header.substring(start, end).trim());
        }

        @Override
        public String toString() {
            return "Part{name=" + getName().orElse("?") +
                   ", filename=" + getFilename().orElse("-") +
                   ", size=" + data.length + "}";
        }
    }

    // -------------------------------------------------------------------------
    // API publique
    // -------------------------------------------------------------------------

    /**
     * Extrait le boundary depuis le header Content-Type de la requête.
     * Retourne Optional.empty() si le Content-Type n'est pas multipart/form-data.
     */
    public static Optional<String> extractBoundary(HttpRequest request) {
        return request.getHeader("content-type").flatMap(ct -> {
            if (!ct.toLowerCase().contains("multipart/form-data")) return Optional.empty();
            int idx = ct.indexOf("boundary=");
            if (idx < 0) return Optional.empty();
            String boundary = ct.substring(idx + 9).trim();
            if (boundary.startsWith("\"")) boundary = boundary.substring(1, boundary.length() - 1);
            return Optional.of(boundary);
        });
    }

    /**
     * Parse le body en une liste de Part.
     *
     * @param body     bytes bruts du body
     * @param boundary boundary extrait du Content-Type
     */
    public static List<Part> parse(byte[] body, String boundary) throws IOException {
        if (body == null || body.length == 0) return List.of();

        byte[] delimiter      = ("--" + boundary).getBytes(StandardCharsets.US_ASCII);
        byte[] closeDelimiter = ("--" + boundary + "--").getBytes(StandardCharsets.US_ASCII);
        byte[] CRLF           = {'\r', '\n'};

        List<Part> parts = new ArrayList<>();
        int pos = 0;

        // Avancer jusqu'au premier delimiter
        pos = indexOf(body, delimiter, pos);
        if (pos < 0) throw new IOException("Multipart boundary not found in body");

        while (pos >= 0) {
            pos += delimiter.length;

            // Fin du multipart ?
            if (pos + 2 <= body.length &&
                body[pos] == '-' && body[pos + 1] == '-') break;

            // Sauter le CRLF après le delimiter
            if (pos + 2 <= body.length &&
                body[pos] == '\r' && body[pos + 1] == '\n') pos += 2;

            // Lire les headers de la part
            int headerEnd = indexOf(body, new byte[]{'\r','\n','\r','\n'}, pos);
            if (headerEnd < 0) break;

            String headerBlock = new String(body, pos, headerEnd - pos, StandardCharsets.US_ASCII);
            Map<String, String> partHeaders = parsePartHeaders(headerBlock);
            pos = headerEnd + 4; // sauter \r\n\r\n

            // Trouver la fin du body de la part (prochain delimiter)
            int nextDelim = indexOf(body, delimiter, pos);
            if (nextDelim < 0) break;

            // Retirer le CRLF qui précède le delimiter
            int dataEnd = nextDelim - 2; // - \r\n
            if (dataEnd > pos) {
                byte[] data = Arrays.copyOfRange(body, pos, dataEnd);
                parts.add(new Part(partHeaders, data));
            }

            pos = nextDelim;
        }

        return Collections.unmodifiableList(parts);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Map<String, String> parsePartHeaders(String block) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String line : block.split("\r\n")) {
            int colon = line.indexOf(':');
            if (colon < 1) continue;
            headers.put(line.substring(0, colon).trim().toLowerCase(),
                        line.substring(colon + 1).trim());
        }
        return headers;
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
}