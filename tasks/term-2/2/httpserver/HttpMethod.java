package httpserver;

public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE;

    public static HttpMethod parse(String value) {
        try {
            return HttpMethod.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) { return null; }
    }
}
