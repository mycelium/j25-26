package httpserver;

import java.util.Objects;

/** Composite lookup key: HTTP method + normalised path. */
final class RouteKey {

    final HttpMethod method;
    final String     path;

    RouteKey(HttpMethod method, String path) {
        this.method = method;
        this.path   = path == null || path.isEmpty() ? "/" : path.startsWith("/") ? path : "/" + path;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof RouteKey r)) return false;
        return method == r.method && Objects.equals(path, r.path);
    }

    @Override public int hashCode() { return Objects.hash(method, path); }
    @Override public String toString() { return method + " " + path; }
}
