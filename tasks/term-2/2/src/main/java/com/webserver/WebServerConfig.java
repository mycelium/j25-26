package com.webserver;

public class WebServerConfig {
    private String host = "localhost";
    private int port = 8081;
    private int threadPoolSize = 10;
    private boolean virtualThreads = false;

    public WebServerConfig host(String host) {
        this.host = host;
        return this;
    }

    public WebServerConfig port(int port) {
        this.port = port;
        return this;
    }

    public WebServerConfig threadPoolSize(int size) {
        this.threadPoolSize = size;
        return this;
    }

    public WebServerConfig virtualThreads(boolean enabled) {
        this.virtualThreads = enabled;
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public boolean isVirtualThreads() {
        return virtualThreads;
    }
}
