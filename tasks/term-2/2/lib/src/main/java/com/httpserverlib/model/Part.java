package com.httpserverlib.model;

import java.nio.charset.StandardCharsets;

/**
 * Represents a part in a multipart/form-data request.
 */
public record Part(String name, String filename, byte[] content, String contentType) {
    public String getContentAsString() { return new String(content, StandardCharsets.UTF_8); }
    public byte[] getContent() { return content.clone(); }
    public boolean isFile() { return filename != null && !filename.isEmpty(); }
}