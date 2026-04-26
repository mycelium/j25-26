package loadtest;

public class Config {
    public static final boolean USE_VIRTUAL_THREADS = false;
    public static final boolean USE_GSON = false;
    public static final int PORT = 8088;
    public static final String HOST = "localhost";
    public static final int THREAD_POOL_SIZE = 10;
    public static final int CONCURRENT_REQUESTS = 50;
    public static final int REQUESTS_PER_CLIENT = 20;
}
