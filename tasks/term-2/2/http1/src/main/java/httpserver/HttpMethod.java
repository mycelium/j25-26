package httpserver;

public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE;

    public static HttpMethod from(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}