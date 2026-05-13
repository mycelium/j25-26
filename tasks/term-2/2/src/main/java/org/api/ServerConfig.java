package org.api;

public record ServerConfig(String host, int port, int threadCount, boolean isVirtual) {

    public ServerConfig {
        if (port < 1 || port > 65535)       throw new IllegalArgumentException("Invalid port: " + port);
        if (threadCount < 1)                 throw new IllegalArgumentException("threadCount must be >= 1");
        if (host == null || host.isBlank())  throw new IllegalArgumentException("host must not be blank");
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String  host        = "localhost";
        private int     port        = 8080;
        private int     threadCount = Runtime.getRuntime().availableProcessors();
        private boolean isVirtual   = false;

        public Builder host(String host)            { this.host        = host;        return this; }
        public Builder port(int port)               { this.port        = port;        return this; }
        public Builder threadCount(int threadCount) { this.threadCount = threadCount; return this; }
        public Builder isVirtual(boolean isVirtual) { this.isVirtual   = isVirtual;   return this; }

        public ServerConfig build() { return new ServerConfig(host, port, threadCount, isVirtual); }
    }
}
