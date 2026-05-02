package httpserver;

public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE;

    public static HttpMethod fromString(String method) {
        try {
            return valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }
}
