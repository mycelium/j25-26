package loadtest;

import java.util.LinkedHashMap;
import java.util.Map;

final class MemoryCatalog {
    private final Map<String, Map<String, Object>> entries;

    private MemoryCatalog(Map<String, Map<String, Object>> entries) {
        this.entries = entries;
    }

    static MemoryCatalog createDefault() {
        Map<String, Map<String, Object>> entries = new LinkedHashMap<>();
        for (int i = 0; i < 1024; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", "item-" + i);
            item.put("multiplier", (i % 11) + 1);
            item.put("bias", i % 7);
            item.put("label", "memory-item-" + i);
            entries.put("item-" + i, item);
        }
        return new MemoryCatalog(Map.copyOf(entries));
    }

    Map<String, Object> byUserId(long userId) {
        long index = Math.floorMod(userId, entries.size());
        return entries.get("item-" + index);
    }
}
