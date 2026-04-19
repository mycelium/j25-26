package loadtest;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public final int port;
    public final boolean virtualThreads;
    public final boolean useGson;
    public final int threadCount;

    public Config(int port, boolean virtualThreads, boolean useGson, int threadCount) {
        this.port = port;
        this.virtualThreads = virtualThreads;
        this.useGson = useGson;
        this.threadCount = threadCount;
    }

    public static Config parse(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.substring(2).split("=", 2);
                if (parts.length == 2) map.put(parts[0], parts[1]);
            }
        }
        return new Config(
            Integer.parseInt(map.getOrDefault("port", "8080")),
            Boolean.parseBoolean(map.getOrDefault("virtual", "false")),
            Boolean.parseBoolean(map.getOrDefault("gson", "false")),
            Integer.parseInt(map.getOrDefault("threads", "10"))
        );
    }
}
