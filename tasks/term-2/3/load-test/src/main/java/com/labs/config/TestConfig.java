package com.labs.config;

public class TestConfig {
    public static final int CONCURRENCY = 50;
    public static final int REQUESTS_PER_THREAD = 100;
    public static final int CLASSIC_THREAD_POOL = 200;
    public static final String HOST = "localhost";
    public static final int PORT = 8080;

    public static final String PAYLOAD_REQ1 = "{\"key\":\"test_key\",\"value\":\"test_value_data\"}";
    public static final String PAYLOAD_REQ2 = "{\"number\":42}";
}