package com.httpserver;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequestParser {

    private static final int MAX_REQUEST_SIZE = 8 * 1024 * 1024; // 8 MB
    private static final byte CR = '\r';
    private static final byte LF = '\n';

    // -------------------------------------------------------------------------
    // Exception interne : connexion fermée proprement par le client
    // -------------------------------------------------------------------------

    public static class EmptyRequestException extends IOException {
        public EmptyRequestException(String message) {
            super(message);
        }
    }

    // -------------------------------------------------------------------------
    // Point d'entrée principal
    // -------------------------------------------------------------------------

    public static HttpRequest parse(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            throw new EmptyRequestException("Empty request data");
        }

        // Chercher \r\n\r\n en bytes (pas de conversion String — charset-safe)
        int headerEnd = findHeaderEnd(data);
        if (headerEnd < 0) {
            throw new IOException("Incomplete request: no header terminator found");
        }
        if (headerEnd == 4) {
            throw new EmptyRequestException("Empty request: only CRLF received");
        }

        // Décoder les headers en US-ASCII (RFC 7230)
        String headerSection = new String(data, 0, headerEnd - 4, StandardCharsets.US_ASCII);
        String[] lines = headerSection.split("\r\n", -1);

        if (lines.length == 0 || lines[0].isBlank()) {
            throw new EmptyRequestException("Empty request line");
        }

        // ── Request line ──────────────────────────────────────────────────────
        String[] parts = lines[0].split(" ", 3);
        if (parts.length < 3) {
            throw new IOException("Invalid request line: " + lines[0]);
        }

        HttpMethod method;
        try {
            method = HttpMethod.from(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new IOException("Unknown HTTP method: " + parts[0]);
        }

        String fullPath = parts[1];
        String version  = parts[2];

        // Valider HTTP/1.1 uniquement
        if (!version.equals("HTTP/1.1")) {
            throw new IOException("Unsupported HTTP version: " + version);
        }

        // ── Path + query params ───────────────────────────────────────────────
        String path = fullPath;
        Map<String, List<String>> queryParams = new LinkedHashMap<>();
        int qIdx = fullPath.indexOf('?');
        if (qIdx > 0) {
            path = fullPath.substring(0, qIdx);
            parseQueryParams(fullPath.substring(qIdx + 1), queryParams);
        }

        // ── Headers — stockés en lowercase (RFC 7230 §3.2) ───────────────────
        Map<String, String> headers = new LinkedHashMap<>();
        int contentLength = 0;

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colon = line.indexOf(':');
            if (colon < 1) continue;

            String name  = line.substring(0, colon).trim().toLowerCase(); // ← lowercase
            String value = line.substring(colon + 1).trim();
            headers.put(name, value);

            if (name.equals("content-length")) {
                try {
                    contentLength = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid Content-Length: " + value);
                }
            }
        }

        // ── Body — lu directement en bytes ────────────────────────────────────
        byte[] body = null;
        if (contentLength > 0) {
            int available = data.length - headerEnd;
            int toRead    = Math.min(contentLength, available);
            body = Arrays.copyOfRange(data, headerEnd, headerEnd + toRead);
        }

        return new HttpRequest(method, path, version, body, headers, queryParams);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Cherche la séquence \r\n\r\n dans les bytes bruts.
     * Retourne l'index du premier octet après la séquence (début du body),
     * ou -1 si non trouvé.
     */
    static int findHeaderEnd(byte[] data) {
        for (int i = 0; i <= data.length - 4; i++) {
            if (data[i] == CR && data[i+1] == LF && data[i+2] == CR && data[i+3] == LF) {
                return i + 4;
            }
        }
        return -1;
    }

    private static void parseQueryParams(String queryString, Map<String, List<String>> out) {
        if (queryString == null || queryString.isEmpty()) return;

        for (String pair : queryString.split("&")) {
            String[] kv = pair.split("=", 2);
            try {
                String key = URLDecoder.decode(kv[0], "UTF-8");
                String val = kv.length > 1 ? URLDecoder.decode(kv[1], "UTF-8") : "";
                out.computeIfAbsent(key, k -> new ArrayList<>()).add(val);
            } catch (UnsupportedEncodingException ignored) {
                // UTF-8 est toujours supporté
            }
        }
    }
}
