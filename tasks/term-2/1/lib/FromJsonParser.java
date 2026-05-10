import java.util.*;

class FromJsonParser {

    private FromJsonParser() {}

    static Object parseToObject(String text, JsonConfig config) {
        JParserJunior jp  = new JParserJunior(text, config);
        Object        res = jp.parseObject();

        jp.skipWhiteSpace();
        if (jp.pos < jp.jsonText.length())
            throw new RuntimeException("Unexpected content after JSON at position " + jp.pos);
        return res;
    }

    static Map<String, Object> parseToMap(String text, JsonConfig config) {
        JParserJunior       jp  = new JParserJunior(text, config);
        Map<String, Object> res = jp.parseMap();

        jp.skipWhiteSpace();
        if (jp.pos < jp.jsonText.length())
            throw new RuntimeException("Unexpected content after JSON at position " + jp.pos);
        return res;
    }

    private static class JParserJunior {
        String     jsonText;
        int        pos;
        JsonConfig config;

        JParserJunior(String aJsonText, JsonConfig aConfig) {
            jsonText = aJsonText;
            config   = aConfig;
        }

        private char next() {
            if (pos >= jsonText.length()) throw new RuntimeException("Parser reached outside of text length");
            return jsonText.charAt(pos++);
        }

        private char get() {
            if (pos >= jsonText.length()) throw new RuntimeException("Parser reached outside of text length");
            return jsonText.charAt(pos);
        }

        Object parseObject() {
            skipWhiteSpace();
            char c = get();
            if (c == '{')                              return parseMap();
            if (c == '[')                              return parseList();
            if (c == '"')                              return parseString();
            if (Character.isDigit(c) || c == '-')     return parseNumber();
            if (c == 't' || c == 'f')                  return parseBoolean();
            if (c == 'n')                              return parseNull();
            throw new RuntimeException("Unexpected character: " + c + " at position " + pos);
        }

        private boolean parseBoolean() {
            StringBuilder sb = new StringBuilder();
            while (pos < jsonText.length() && Character.isAlphabetic(get())) { sb.append(next()); }
            String res = sb.toString();
            if (!(res.equals("true") || res.equals("false")))
                throw new RuntimeException("Invalid value " + res);
            return res.equals("true");
        }

        private Object parseNull() {
            StringBuilder sb = new StringBuilder();
            while (pos < jsonText.length() && Character.isAlphabetic(get())) { sb.append(next()); }
            String res = sb.toString();
            if (!res.equals("null")) throw new RuntimeException("Invalid value " + res);
            return null;
        }

        String parseString() {
            next();
            StringBuilder sb = new StringBuilder();
            while (true) {
                char ch = next();
                if (ch == '"') break;
                if (ch == '\\') {
                    char escaped = next();
                    switch (escaped) {
                        case '"'  -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/'  -> sb.append('/');
                        case 'b'  -> sb.append('\b');
                        case 'f'  -> sb.append('\f');
                        case 'n'  -> sb.append('\n');
                        case 'r'  -> sb.append('\r');
                        case 't'  -> sb.append('\t');
                        case 'u'  -> {
                            if (pos + 4 > jsonText.length())
                                throw new RuntimeException("Invalid \\uXXXX escape at position " + pos);
                            String hex = jsonText.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                        default -> throw new RuntimeException("Invalid escape: \\" + escaped);
                    }
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        private Number parseNumber() {
            StringBuilder sb = new StringBuilder();
            if (pos < jsonText.length() && get() == '-') { sb.append('-'); next(); }
            while (pos < jsonText.length() && Character.isDigit(get())) sb.append(next());

            if (pos < jsonText.length() && get() == '.') {
                sb.append(next());
                while (pos < jsonText.length() && Character.isDigit(get())) sb.append(next());
                return parseFloating(sb);
            }
            if (pos < jsonText.length() && (get() == 'e' || get() == 'E')) {
                return parseFloating(sb);
            }

            try { return Integer.parseInt(sb.toString());
            } catch (NumberFormatException e) {
                try { return Long.parseLong(sb.toString());
                } catch (NumberFormatException e2) {
                    throw new RuntimeException("Invalid number: " + sb);
                }
            }
        }

        private double parseFloating(StringBuilder sb) {
            if (pos < jsonText.length() && (get() == 'e' || get() == 'E')) {
                sb.append(next());
                if (pos < jsonText.length() && (get() == '+' || get() == '-')) sb.append(next());
                while (pos < jsonText.length() && Character.isDigit(get())) sb.append(next());
            }
            return Double.parseDouble(sb.toString());
        }

        List<Object> parseList() {
            next();
            skipWhiteSpace();
            List<Object> res = new ArrayList<>();
            if (get() == ']') { next(); return res; }

            while (true) {
                skipWhiteSpace();
                res.add(parseObject());
                skipWhiteSpace();
                var nextCh = next();
                if      (nextCh == ']') break;
                else if (nextCh != ',') throw new RuntimeException("Comma expected at position " + pos);
            }
            return res;
        }

        void skipWhiteSpace() {
            while (pos < jsonText.length() && Character.isWhitespace(get())) pos++;
        }

        Map<String, Object> parseMap() {
            next();
            skipWhiteSpace();
            Map<String, Object> res = new HashMap<>();
            if (get() == '}') { next(); return res; }

            while (true) {
                skipWhiteSpace();
                String key = parseString();
                skipWhiteSpace();
                if (next() != ':') throw new RuntimeException(": expected at position " + pos);

                if (res.containsKey(key))
                    if (config.failOnDuplicateKeys)
                        throw new RuntimeException("Key " + key + " is present twice.");

                res.put(key, parseObject());

                skipWhiteSpace();
                var nextCh = next();
                if      (nextCh == '}') break;
                else if (nextCh != ',') throw new RuntimeException("Comma expected at position " + pos);
            }
            return res;
        }
    }
}