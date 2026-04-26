package loadtest;

import java.util.ArrayList;
import java.util.List;

public class Stats {
    private final List<Long> times = new ArrayList<>();
    private long totalTime = 0;
    private int successCount = 0;
    private int failCount = 0;
    private long minTime = Long.MAX_VALUE;
    private long maxTime = 0;
    
    public synchronized void add(long timeMs, boolean success) {
        times.add(timeMs);
        if (success) {
            successCount++;
            totalTime += timeMs;
            if (timeMs < minTime) minTime = timeMs;
            if (timeMs > maxTime) maxTime = timeMs;
        } else {
            failCount++;
        }
    }
    
    public double getAvgTime() { return successCount > 0 ? (double) totalTime / successCount : 0; }
    public long getMinTime() { return minTime == Long.MAX_VALUE ? 0 : minTime; }
    public long getMaxTime() { return maxTime; }
    public int getSuccessCount() { return successCount; }
    public int getFailCount() { return failCount; }
    
    public void print(String name) {
        System.out.println("========== " + name + " ==========");
        System.out.println("Success: " + successCount);
        System.out.println("Errors: " + failCount);
        System.out.println("Avg time: " + String.format("%.2f", getAvgTime()) + " ms");
        System.out.println("Min time: " + getMinTime() + " ms");
        System.out.println("Max time: " + getMaxTime() + " ms");
    }
}
