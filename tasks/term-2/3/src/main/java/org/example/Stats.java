package org.example;

import java.util.concurrent.atomic.AtomicLong;

public class Stats {

    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failCount    = new AtomicLong(0);
    private final AtomicLong totalMs      = new AtomicLong(0);

    private volatile long minMs = Long.MAX_VALUE;
    private volatile long maxMs = Long.MIN_VALUE;

    public void record(long elapsedMs, boolean success) {
        if (success) successCount.incrementAndGet();
        else         failCount.incrementAndGet();

        totalMs.addAndGet(elapsedMs);

        synchronized (this) {
            if (elapsedMs < minMs) minMs = elapsedMs;
            if (elapsedMs > maxMs) maxMs = elapsedMs;
        }
    }

    public long   successCount() { return successCount.get(); }
    public long   failCount()    { return failCount.get(); }
    public long   totalCount()   { return successCount.get() + failCount.get(); }
    public double avgMs()        { long t = totalCount(); return t == 0 ? 0 : (double) totalMs.get() / t; }
    public long   minMs()        { return minMs == Long.MAX_VALUE ? 0 : minMs; }
    public long   maxMs()        { return maxMs == Long.MIN_VALUE ? 0 : maxMs; }

    @Override
    public String toString() {
        return String.format("avg=%.2f ms  min=%d ms  max=%d ms  ok=%d  fail=%d",
                avgMs(), minMs(), maxMs(), successCount(), failCount());
    }
}
