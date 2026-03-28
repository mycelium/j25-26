package httpserver;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE;

    static HttpMethod fromToken(String token) {
        for (HttpMethod method : values()) {
            if (method.name().equals(token)) {
                return method;
            }
        }
        return null;
    }
}
