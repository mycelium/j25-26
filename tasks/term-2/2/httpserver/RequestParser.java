package httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    public static HttpRequest parse(SocketChannel channel) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(channel.socket().getInputStream(), StandardCharsets.UTF_8));
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request");
        }
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 3) {
            throw new IOException("Invalid request line: " + requestLine);
        }

        RequestMethod method = RequestMethod.valueOf(requestParts[0].toUpperCase());
        String fullPath = requestParts[1];

        String path;
        Map<String, String> queryParams = new HashMap<>();
        int queryIndex = fullPath.indexOf('?');

        if (queryIndex >= 0) {
            path = fullPath.substring(0, queryIndex);
            String queryString = fullPath.substring(queryIndex + 1);
            queryParams = parseQueryParams(queryString);
        } else {
            path = fullPath;
        }

        Map<String, String> headers = new HashMap<>();
        int contentLength = 0;
        String headerLine;

        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            int colonIndex = headerLine.indexOf(':');
            if (colonIndex > 0) {
                String headerName = headerLine.substring(0, colonIndex).trim().toLowerCase();
                String headerValue = headerLine.substring(colonIndex + 1).trim();
                headers.put(headerName, headerValue);

                if (headerName.equals("content-length")) {
                    contentLength = Integer.parseInt(headerValue);
                }
            }
        }

        byte[] body = new byte[0];
        if (contentLength > 0) {
            char[] bodyChars = new char[contentLength];
            int read = reader.read(bodyChars, 0, contentLength);
            if (read != contentLength) {
                throw new IOException("Incomplete body read. Expected: " + contentLength + ", got: " + read);
            }
            body = new String(bodyChars).getBytes(StandardCharsets.UTF_8);
        }

        Map<String, String> formData = parseFormData(headers, body);

        return new HttpRequest(method, path, headers, body, queryParams, formData);
    }

    private static Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = urlDecode(keyValue[0]);
            String value = keyValue.length > 1 ? urlDecode(keyValue[1]) : "";
            params.put(key, value);
        }
        return params;
    }

    private static Map<String, String> parseFormData(Map<String, String> headers, byte[] body) {
        String contentType = headers.get("content-type");
        if (contentType == null || body.length == 0) {
            return new HashMap<>();
        }

        if (contentType.contains("application/x-www-form-urlencoded")) {
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            return parseQueryParams(bodyStr);
        }

        if (contentType.contains("multipart/form-data")) {
            String boundary = extractBoundary(contentType);
            if (boundary != null) {
                return parseMultipartFormData(body, boundary);
            }
        }

        return new HashMap<>();
    }

    private static String extractBoundary(String contentType) {
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return part.substring("boundary=".length());
            }
        }
        return null;
    }

    private static Map<String, String> parseMultipartFormData(byte[] body, String boundary) {
        Map<String, String> formData = new HashMap<>();
        String bodyStr = new String(body, StandardCharsets.UTF_8);
        String delimiter = "--" + boundary;

        String[] parts = bodyStr.split(delimiter);

        for (String part : parts) {
            if (part.trim().isEmpty() || part.equals("--") || part.equals("--\r\n")) {
                continue;
            }

            int headerEndIndex = part.indexOf("\r\n\r\n");
            if (headerEndIndex < 0) {
                continue;
            }

            String partHeaders = part.substring(0, headerEndIndex);
            String partContent = part.substring(headerEndIndex + 4);

            if (partContent.endsWith("\r\n")) {
                partContent = partContent.substring(0, partContent.length() - 2);
            }

            String fieldName = extractFieldName(partHeaders);

            String fileName = extractFileName(partHeaders);

            if (fieldName != null) {
                if (fileName != null) {
                    formData.put(fieldName + "_filename", fileName);
                    formData.put(fieldName, partContent);
                } else {
                    formData.put(fieldName, partContent.trim());
                }
            }
        }
        return formData;
    }

    private static String extractFieldName(String headers) {
        String[] lines = headers.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("Content-Disposition:")) {
                int nameIndex = line.indexOf("name=\"");
                if (nameIndex >= 0) {
                    int endIndex = line.indexOf("\"", nameIndex + 6);
                    if (endIndex > nameIndex) {
                        return line.substring(nameIndex + 6, endIndex);
                    }
                }
            }
        }
        return null;
    }

    private static String extractFileName(String headers) {
        String[] lines = headers.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("Content-Disposition:")) {
                int filenameIndex = line.indexOf("filename=\"");
                if (filenameIndex >= 0) {
                    int endIndex = line.indexOf("\"", filenameIndex + 10);
                    if (endIndex > filenameIndex) {
                        return line.substring(filenameIndex + 10, endIndex);
                    }
                }
            }
        }
        return null;
    }

    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}