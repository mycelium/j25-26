package jsonlib;


final class JsonWriter {

    private JsonWriter() {}

    
    static String serializeNumber(Number n) {
        if (n instanceof Double d) {
            if (Double.isInfinite(d) || Double.isNaN(d)) return "null";
            if (d == Math.floor(d) && Math.abs(d) < 1e15) return Long.toString(d.longValue());
            return d.toString();
        }
        if (n instanceof Float f) {
            if (Float.isInfinite(f) || Float.isNaN(f)) return "null";
            if (f == Math.floor(f) && Math.abs(f) < 1e7) return Long.toString(f.longValue());
            return f.toString();
        }
        return n.toString();
    }

   

    static String escape(String s) {
        // Fast path — most strings need no escaping
        boolean needsEscape = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\' || c < 0x20) { needsEscape = true; break; }
        }
        if (!needsEscape) return s;

        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
