package server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServerBuilder {
    private String host = "localhost";
    private int port = 8080;
    private int threadCount = 10;
    private boolean isVirtual = false;

    public HttpServerBuilder host(String host) {
        this.host = host;
        return this;
    }

    public HttpServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public HttpServerBuilder threadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public HttpServerBuilder useVirtualThreads(boolean isVirtual) {
        this.isVirtual = isVirtual;
        return this;
    }

    public HttpServer build() {
        ExecutorService executor = isVirtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(threadCount);
        return new HttpServer(host, port, executor);
    }
}
