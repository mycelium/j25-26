package com.labs.client;

public class TestResult {
    public final String endpoint;
    public final double avgTimeMs;
    public final long totalRequests;
    public final long totalBytes;
    public final long timestamp;

    public TestResult(String endpoint, double avgTimeMs, long totalRequests, long totalBytes) {
        this.endpoint = endpoint;
        this.avgTimeMs = avgTimeMs;
        this.totalRequests = totalRequests;
        this.totalBytes = totalBytes;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("TestResult{endpoint='%s', avgTime=%.2f ms, requests=%d, bytes=%d}",
                endpoint, avgTimeMs, totalRequests, totalBytes);
    }
}