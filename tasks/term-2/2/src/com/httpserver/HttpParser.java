package com.httpserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpParser {
    
    public static HttpRequest parse(byte[] data) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
        
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request");
        }
        
        String[] parts = requestLine.split(" ");
        if (parts.length < 3) {
            throw new IOException("Invalid request line: " + requestLine);
        }
        
        String method = parts[0];
        String fullPath = parts[1];
        
        String path = fullPath;
        Map<String, List<String>> queryParams = new HashMap<>();
        int qmIdx = fullPath.indexOf('?');
        if (qmIdx > 0) {
            path = fullPath.substring(0, qmIdx);
            String queryString = fullPath.substring(qmIdx + 1);
            parseQueryParams(queryString, queryParams);
        }
        
        Map<String, String> headers = new HashMap<>();
        String line;
        int contentLength = 0;
        
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                String name = line.substring(0, colonIdx).trim();
                String value = line.substring(colonIdx + 1).trim();
                headers.put(name, value);
                if (name.equalsIgnoreCase("Content-Length")) {
                    contentLength = Integer.parseInt(value);
                }
            }
        }
        
        byte[] body = null;
        if (contentLength > 0) {
            char[] bodyChars = new char[contentLength];
            int read = reader.read(bodyChars, 0, contentLength);
            if (read > 0) {
                body = new String(bodyChars, 0, read).getBytes();
            }
        }
        
        HttpRequest request = new HttpRequest(method, path, body);
        request.headers.putAll(headers);
        request.queryParams.putAll(queryParams);
        
        return request;
    }
    
    private static void parseQueryParams(String queryString, Map<String, List<String>> params) {
        if (queryString == null || queryString.isEmpty()) return;
        
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=", 2);
            try {
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
                params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            } catch (UnsupportedEncodingException e) {
                // Ignorer
            }
        }
    }
}