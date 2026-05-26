package com.httpserver;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public record RequestPacket(
        String httpMethod,
        String uriPath,
        Map<String, String> headerFields,
        byte[] rawBody
) {
    public String getBodyText() {
        return new String(rawBody, StandardCharsets.UTF_8);
    }

    public byte[] getBodyRaw() {
        return rawBody;
    }
}