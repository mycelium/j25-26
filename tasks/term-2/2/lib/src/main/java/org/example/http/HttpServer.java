package org.example.http;

import java.util.HashMap;
import java.util.Map;


public class HttpServer {

    private final HttpServerImpl impl;

    private HttpServer(HttpServerImpl impl) {
        this.impl = impl;
    }

    public void start() {
        impl.start();
    }

    public void stop() {
        impl.stop();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String host = "0.0.0.0";
        private int port = 8080;
        private int threads = Runtime.getRuntime().availableProcessors();
        private boolean isVirtual = false;
        private final Map<String, Map<HttpMethod, HttpHandler>> routes = new HashMap<>();

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder isVirtual(boolean isVirtual) {
            this.isVirtual = isVirtual;
            return this;
        }

        public Builder route(String path, HttpMethod method, HttpHandler handler) {
            routes.computeIfAbsent(path, k -> new HashMap<>()).put(method, handler);
            return this;
        }

        public HttpServer build() {
            return new HttpServer(new HttpServerImpl(host, port, threads, isVirtual, routes));
        }
    }
}
