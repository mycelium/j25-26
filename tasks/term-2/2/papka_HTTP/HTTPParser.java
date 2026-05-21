package papka_HTTP;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HTTPParser {

    public static HTTPRequest parse(InputStream inputStream) throws IOException {
        String requestLine = readLine(inputStream);
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ");
        String methodStr = parts[0];
        String fullPath = parts[1];

        String path;
        Map<String, String> parameters = new HashMap<>();

        int questionIndex = fullPath.indexOf('?');
        if (questionIndex != -1) {
            path = fullPath.substring(0, questionIndex);
            String queryString = fullPath.substring(questionIndex + 1);
            parameters = parseQueryString(queryString);
        } else {
            path = fullPath;
        }

        Map<String, String> headers = readHeaders(inputStream);

        String body = readBody(inputStream, headers);

        return new HTTPRequest(
                AllMethods.valueOf(methodStr),
                path,
                headers,
                parameters,
                body.getBytes()
        );
    }

    private static String readLine(InputStream inputStream) throws IOException {
        StringBuilder line = new StringBuilder();
        int prev = -1;
        int curr;

        while ((curr = inputStream.read()) != -1) {
            if (prev == '\r' && curr == '\n') {

                if (line.length() > 0) {
                    line.setLength(line.length() - 1);
                }
                return line.toString();
            }
            line.append((char) curr);
            prev = curr;
        }

        return line.length() > 0 ? line.toString() : null;
    }


    private static Map<String, String> readHeaders(InputStream inputStream) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;

        while ((line = readLine(inputStream)) != null && !line.isEmpty()) {
            String[] parts = line.split(": ", 2);
            if (parts.length == 2) {
                headers.put(parts[0].toLowerCase(), parts[1]);
            }
        }

        return headers;
    }


    private static String readBody(InputStream inputStream, Map<String, String> headers) throws IOException {
        String contentLength = headers.get("content-length");

        if (contentLength != null) {
            int length = Integer.parseInt(contentLength);
            byte[] body = new byte[length];
            inputStream.read(body);
            return new String(body, StandardCharsets.UTF_8);
        }

        return "";
    }


    private static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> parameters = new HashMap<>();

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                parameters.put(keyValue[0], keyValue[1]);
            }
        }

        return parameters;
    }
}
