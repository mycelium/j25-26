package loadtest;

public class Config {
    // Server settings
    public static final int PORT = 8088;
    public static final String HOST = "localhost";
    public static final int THREAD_POOL_SIZE = 10;
    
    // Test settings
    public static final int WARMUP_REQUESTS = 100;
    public static final int CONCURRENT_REQUESTS = 50;
    public static final int REQUESTS_PER_CLIENT = 20;
    
    // Configuration flags (will be set via command line)
    public static boolean USE_VIRTUAL_THREADS = false;
    public static boolean USE_GSON = false;
}
