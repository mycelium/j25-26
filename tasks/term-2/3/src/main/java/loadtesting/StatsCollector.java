package loadtesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thread-safe collector for request latencies.
 * Computes avg, min, max after all requests finish.
 */
public class StatsCollector {

    private final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

    public void record(long ms) {
        latencies.add(ms);
    }

    public Summary summarize(int totalRequests, int errorCount, long wallMs) {
        List<Long> sorted = latencies.stream()
                .filter(l -> l > 0)
                .sorted()
                .toList();

        if (sorted.isEmpty()) {
            return new Summary(0, 0, 0, wallMs, errorCount, totalRequests);
        }

        long avg = (long) sorted.stream().mapToLong(Long::longValue).average().orElse(0);
        long min = sorted.get(0);
        long max = sorted.get(sorted.size() - 1);

        return new Summary(avg, min, max, wallMs, errorCount, totalRequests);
    }

    public record Summary(
            long avgMs,
            long minMs,
            long maxMs,
            long wallMs,
            int  errors,
            int  total
    ) {
        @Override
        public String toString() {
            return String.format(
                    "avg=%dms  min=%dms  max=%dms  wall=%dms  errors=%d/%d",
                    avgMs, minMs, maxMs, wallMs, errors, total);
        }
    }
}