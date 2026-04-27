package com.httpserverlib.config;

public record ServerConfig(String host, int port, int threadPoolSize, boolean useVirtualThreads) {}