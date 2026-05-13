package ru.task2.http;

import java.util.Objects;

public class RouteKey {

    private final HttpMethod method;
    private final String path;

    public RouteKey(HttpMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteKey that)) return false;
        return method == that.method &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}