package com.httpserverlib.multipart;

import com.httpserverlib.model.Part;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class MultipartParser {

    public static List<Part> parse(byte[] body, String boundary) {
        List<Part> parts = new ArrayList<>();
        if (body == null || body.length == 0) return parts;

        // Convert boundary to byte array for fast comparison
        byte[] boundaryBytes = boundary.getBytes(StandardCharsets.US_ASCII);
        byte[] endBoundaryBytes = (boundary + "--").getBytes(StandardCharsets.US_ASCII);

        int pos = 0;
        int length = body.length;

        // Find the first boundary
        pos = indexOf(body, boundaryBytes, pos);
        if (pos == -1) return parts;
        pos += boundaryBytes.length;

        // Skip optional CRLF after boundary
        if (pos < length && (body[pos] == '\r' || body[pos] == '\n')) {
            if (body[pos] == '\r') pos++;
            if (pos < length && body[pos] == '\n') pos++;
        }

        while (pos < length) {
            // Find next boundary (or end boundary)
            int nextBoundary = indexOf(body, boundaryBytes, pos);
            int nextEndBoundary = indexOf(body, endBoundaryBytes, pos);

            int endPos;
            boolean isLast = false;

            if (nextEndBoundary != -1 && (nextBoundary == -1 || nextEndBoundary < nextBoundary)) {
                endPos = nextEndBoundary;
                isLast = true;
            } else if (nextBoundary != -1) {
                endPos = nextBoundary;
            } else {
                break;
            }

            // Extract part content (from pos to endPos, then trim trailing CRLF)
            int partStart = pos;
            int partEnd = endPos;

            // Trim trailing CRLF from part data
            if (partEnd > partStart && body[partEnd - 2] == '\r' && body[partEnd - 1] == '\n') {
                partEnd -= 2;
            } else if (partEnd > partStart && body[partEnd - 1] == '\n') {
                partEnd -= 1;
            }

            byte[] partData = Arrays.copyOfRange(body, partStart, partEnd);

            // Parse headers and body of this part
            int headerEnd = findHeaderEnd(partData);
            if (headerEnd == -1) {
                pos = endPos + (isLast ? endBoundaryBytes.length : boundaryBytes.length);
                continue;
            }

            // Headers as string
            String headersStr = new String(partData, 0, headerEnd, StandardCharsets.ISO_8859_1);
            // Body starts after double CRLF (headerEnd advances)
            byte[] content = Arrays.copyOfRange(partData, headerEnd + 2, partData.length);

            // Parse headers
            Map<String, String> headers = new HashMap<>();
            for (String line : headersStr.split("\\r?\\n")) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    String name = line.substring(0, colon).trim().toLowerCase();
                    String value = line.substring(colon + 1).trim();
                    headers.put(name, value);
                }
            }

            String disposition = headers.get("content-disposition");
            if (disposition != null && disposition.startsWith("form-data")) {
                String name = extractParam(disposition, "name");
                String filename = extractParam(disposition, "filename");
                String contentType = headers.get("content-type");
                Part part = new Part(name, filename, content, contentType);
                parts.add(part);
            }

            // Move position after the boundary we just processed
            pos = endPos + (isLast ? endBoundaryBytes.length : boundaryBytes.length);
            // Skip CRLF after boundary
            if (pos < length && body[pos] == '\r') pos++;
            if (pos < length && body[pos] == '\n') pos++;

            if (isLast) break;
        }

        return parts;
    }

    private static int indexOf(byte[] data, byte[] pattern, int start) {
        outer: for (int i = start; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static int findHeaderEnd(byte[] data) {
        // Search for \r\n\r\n or \n\n
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i+1] == '\n' && data[i+2] == '\r' && data[i+3] == '\n') {
                return i; // position before the double CRLF
            }
            if (data[i] == '\n' && data[i+1] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private static String extractParam(String disposition, String paramName) {
        String pattern = paramName + "=\"";
        int idx = disposition.indexOf(pattern);
        if (idx < 0) return null;
        int start = idx + pattern.length();
        int end = disposition.indexOf('"', start);
        if (end < 0) return null;
        return disposition.substring(start, end);
    }
}