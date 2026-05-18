package org.example.loadtest;

public class Config {
    public final boolean isVirtual;
    public final boolean useOwnParser;
    public final int serverThreads;
    public final int clientThreads;
    public final int requestsPerThread;
    public final int serverPort;

    public Config(boolean isVirtual, boolean useOwnParser, int serverThreads,
                  int clientThreads, int requestsPerThread, int serverPort) {
        this.isVirtual = isVirtual;
        this.useOwnParser = useOwnParser;
        this.serverThreads = serverThreads;
        this.clientThreads = clientThreads;
        this.requestsPerThread = requestsPerThread;
        this.serverPort = serverPort;
    }

    public String name() {
        return (isVirtual ? "Virtual" : "Classic") + " + " + (useOwnParser ? "Own Parser" : "Gson");
    }
}
