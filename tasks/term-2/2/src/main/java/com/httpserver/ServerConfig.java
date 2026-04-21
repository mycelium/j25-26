package com.httpserver;

public class ServerConfig {
    private String host = "localhost";
    private int port = 8081;
    private int threadCount = 10;
    private boolean useVirtualThreads = false;

    public ServerConfig host(String host) { this.host = host; return this; }
    public ServerConfig port(int port) { this.port = port; return this; }
    public ServerConfig threadCount(int count) { this.threadCount = count; return this; }
    public ServerConfig useVirtualThreads(boolean use) { this.useVirtualThreads = use; return this; }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public int getThreadCount() { return threadCount; }
    public boolean isUseVirtualThreads() { return useVirtualThreads; }
}