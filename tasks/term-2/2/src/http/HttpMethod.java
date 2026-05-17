package http;

public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE;

    public static HttpMethod from(String raw) {
        return HttpMethod.valueOf(raw.trim().toUpperCase());
    }
}
