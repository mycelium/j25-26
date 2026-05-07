import java.math.BigDecimal;
import java.util.*;

public class FromJsonParser {

    private void fromJsonParser(){};

    private static class JParserJunior{
        String jsonText;
        int    pos;

        JParserJunior(String aJsonText) { jsonText = aJsonText; }

        private Character next() {
            if (pos >= jsonText.length()) throw new RuntimeException("Parser reached outside of text length");
            return jsonText.charAt(pos++);
        }

        private Character get(){
            if (pos >= jsonText.length()) throw new RuntimeException("Parser reached outside of text length");
            return jsonText.charAt(pos);
        }

        Object parseObject(){
            skipWhiteSpace();
            return switch (get()){
                case '{'                                               -> parseMap();
                case '['                                               -> parseList();
                case '"'                                               -> parseString();
                case Character c when Character.isDigit(c) || c == '-' -> parseNumber();
                case 't', 'f'                                          -> parseBoolean();
                case 'n'                                               -> parseNull();
                default -> null;
            };
        }

        private boolean parseBoolean(){
            StringBuilder sb = new StringBuilder();
            while (pos < jsonText.length() && Character.isAlphabetic(get())){ sb.append(next()); }
            String res = sb.toString();
            if (!(res.equals("true") || res.equals("false")))
                throw new RuntimeException("Invalid value " + res);
            return res.equals("true");
        }

        private Object parseNull(){
            StringBuilder sb = new StringBuilder();
            while (pos < jsonText.length() && Character.isAlphabetic(get())){ sb.append(next()); }
            String res = sb.toString();
            if (!res.equals("null")) throw new RuntimeException("Invalid value " + res);
            return null;
        }

        String parseString(){
            next();
            StringBuilder sb = new StringBuilder();
            while(true){
                Character ch = next();
                if (ch == '"') break;
                if (ch == '\\') {
                    Character escaped = next();
                    switch (escaped) {
                        case '"'  -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/'  -> sb.append('/');
                        case 'b'  -> sb.append('\b');
                        case 'f'  -> sb.append('\f');
                        case 'n'  -> sb.append('\n');
                        case 'r'  -> sb.append('\r');
                        case 't'  -> sb.append('\t');
                        default   -> throw new RuntimeException("Invalid escape: \\" + escaped);
                    }
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        private Object parseNumber(){
            StringBuilder sb = new StringBuilder();
            if (pos < jsonText.length() && get() == '-') {
                sb.append('-');
                next();
            }
            while (pos < jsonText.length() && Character.isDigit(get()))  sb.append(next());
            if    (pos < jsonText.length() && get() == '.')              return parseDouble(sb);
            try { return Integer.valueOf(sb.toString());
            } catch (NumberFormatException e){
                try { return Short.valueOf(sb.toString());
            } catch (NumberFormatException e2){
                try { return Integer.valueOf(sb.toString());
            } catch (NumberFormatException e3) {
                try { return Long.valueOf(sb.toString());
            } catch (NumberFormatException e4) {
                    return new BigDecimal(sb.toString());
            }}}}}

        Object parseDouble(StringBuilder sb){
            next();
            sb.append('.');
            while (pos < jsonText.length() && Character.isDigit(get())){ sb.append(next()); }
            try {
                return Float.valueOf(sb.toString());
            } catch (NumberFormatException e){
                return Double.valueOf(sb.toString());
            }
        }

        List<Object> parseList(){
            next();
            skipWhiteSpace();
            List<Object> res = new ArrayList<>();
            if (get() == ']') return res;

            while (true){
                skipWhiteSpace();
                res.add(parseObject());
                skipWhiteSpace();
                var nextCh = next();
                if      (nextCh == ']') break;
                else if (nextCh != ',') throw new RuntimeException("Comma expected at position " + pos);
            }
            return res;
        }

        private void skipWhiteSpace(){
            while (pos < jsonText.length() && Character.isWhitespace(get())) pos++;
        }

        Map<String, Object> parseMap(){
            next();
            skipWhiteSpace();
            Map<String, Object> res = new HashMap<>();
            if (get() == '}') return res;

            while(true) {
                skipWhiteSpace();
                String key = parseString();
                skipWhiteSpace();
                if      (next() != ':')        throw new RuntimeException(": expected at position " + pos);
                else if (res.containsKey(key)) throw new RuntimeException("Key " + key + " is present twice.");

                res.put(key, parseObject());

                skipWhiteSpace();
                var nextCh = next();
                if      (nextCh == '}') break;
                else if (nextCh != ',') throw new RuntimeException("Comma expected at position " + pos);
            }
            return res;
        }
    }

    public static Object parseToObject(String text){
        JParserJunior jp  = new JParserJunior(text);
        Object        res = jp.parseObject();

        jp.skipWhiteSpace();
        if (jp.pos < jp.jsonText.length())
            throw new RuntimeException("Unexpected content after JSON at position " + jp.pos);
        return res;
    }

    public static Map<String, Object> parseToMap(String text){
        JParserJunior       jp  = new JParserJunior(text);
        Map<String, Object> res = jp.parseMap();

        jp.skipWhiteSpace();
        if (jp.pos < jp.jsonText.length())
            throw new RuntimeException("Unexpected content after JSON at position " + jp.pos);
        return res;
    }


    public static <T> T parseToClass(String text, Class<T> cls){
        return JsonCast.convert(parseToObject(text), cls);
    }
}
