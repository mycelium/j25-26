package papkaJSON;
import java.util.*;

public class ParserJSON {

    private String strJSON;
    private int i;


    public Object parse(String str_JSON)
    {
        this.strJSON = str_JSON;
        this.i = 0;

        skipWhitespace();

        if (i >= strJSON.length()) {
            throw new RuntimeException("Error");
        }
        return parseValue();
    }

    private Map<String, Object> parseObj()
    {
        Map<String, Object> res = new LinkedHashMap<>();
        skipWhitespace();
        validateChar('{');

        if (strJSON.charAt(i) == '}') {
            i++;
            return res;
        }

        while (true)
        {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            validateChar(':');
            skipWhitespace();
            Object value = parseValue();
            res.put(key, value);
            skipWhitespace();
            char c = strJSON.charAt(i);
            if (c == '}') {
                i++;
                break;  // конец объекта
            }
            else if (c == ',') {
                i++;
            }
            else {
                throw new RuntimeException("Error");
            }
        }

        return res;
    }



    private void skipWhitespace()
    {
        while (i < strJSON.length() && Character.isWhitespace(strJSON.charAt(i)))
        {
            i++;
        }
    }

    private void validateChar(char c) {
        if(strJSON.charAt(i) != c)
        {
            throw new RuntimeException("Error parse");
        }
        i++;
    }

    private String parseString()
    {
        skipWhitespace();
        validateChar('"' );
        StringBuilder strSB = new StringBuilder();
        while(true) {
            char c = strJSON.charAt(i);
            if(c == '"') {
                i++;
                break;
            }
            else if(c == '\\') {
                i++;
                c = strJSON.charAt(i);
                switch (c) {
                    case '"':
                        strSB.append('"');
                        break;
                    case '\\':
                        strSB.append('\\');
                        break;
                    case 'b':
                        strSB.append('\b');
                        break;
                    case 'f':
                        strSB.append('\f');
                        break;
                    case 'n':
                        strSB.append('\n');
                        break;
                    case 'r':
                        strSB.append('\r');
                        break;
                    case 't':
                        strSB.append('\t');
                        break;
                    default:
                        throw new RuntimeException("Error");
                }
                i++;
            }
            else {
                strSB.append(c);
                i++;
            }
        }

        return strSB.toString();
    }

    private Object parseValue() {
        skipWhitespace();
        char c = strJSON.charAt(i);
        switch (c) {
            case '"':
                return parseString();
            case '{':
                return parseObj();
            case '[':
                return parseArray();
            case 't':
                return parseBool();
            case 'f':
                return parseBool();
            case 'n':
                return parseNull();
            default:
                if (Character.isDigit(c) || c == '-') {
                    return parseNumber();
                }
                throw new RuntimeException("What? Error!");
        }
    }


    private List<Object> parseArray() {
        List<Object> lObj = new ArrayList<>();

        validateChar('[');
        skipWhitespace();

        if (strJSON.charAt(i) == ']') {
            i++;
            return lObj;
        }
        while (true) {
            Object val = parseValue();
            lObj.add(val);
            skipWhitespace();

            char c = strJSON.charAt(i);
            if (c == ']') {
                i++;
                break;
            }
            else if (c == ',') {
                i++;
                skipWhitespace();
            }
            else {
                throw new RuntimeException("Error");
            }
        }
        return lObj;
    }

    private Boolean parseBool() {
        if (strJSON.charAt(i) == 't' && strJSON.charAt(i + 1) == 'r' && strJSON.charAt(i + 2) == 'u' &&
                strJSON.charAt(i + 3) == 'e') {
            i += 4;
            return true;
        }
        else if (strJSON.charAt(i) == 'f' && strJSON.charAt(i + 1) == 'a' && strJSON.charAt(i + 2) == 'l' &&
                strJSON.charAt(i + 3) == 's' && strJSON.charAt(i + 4) == 'e') {

            i += 5;
            return false;
        }
        else {
            throw new RuntimeException("Error");
        }
    }

    private Object parseNull() {
        if (i + 3 < strJSON.length() && strJSON.charAt(i) == 'n' && strJSON.charAt(i + 1) == 'u' &&
                strJSON.charAt(i + 2) == 'l' && strJSON.charAt(i + 3) == 'l') {
            i += 4;
            return null;
        }
        else {
            throw new RuntimeException("Error");
        }

    }

    private Number parseNumber() {
        int start = i;

        if (i < strJSON.length() && strJSON.charAt(i) == '-') {
            i++;
        }

        if (i >= strJSON.length() || !Character.isDigit(strJSON.charAt(i))) {
            throw new RuntimeException("Error");
        }

        while (i < strJSON.length() && Character.isDigit(strJSON.charAt(i))) {
            i++;
        }

        if (i < strJSON.length() && strJSON.charAt(i) == '.') {
            i++;
            while (i < strJSON.length() && Character.isDigit(strJSON.charAt(i))) {
                i++;
            }
        }
        if (i < strJSON.length() && (strJSON.charAt(i) == 'e' || strJSON.charAt(i) == 'E')) {
            i++;

            if (i < strJSON.length() && (strJSON.charAt(i) == '+' || strJSON.charAt(i) == '-')) {
                i++;
            }
            while (i < strJSON.length() && Character.isDigit(strJSON.charAt(i))) {
                i++;
            }
        }
        String number = strJSON.substring(start, i);
        try {
            if (number.contains(".") || number.contains("e") || number.contains("E")) {
                return Double.parseDouble(number);
            } else {
                long longN = Long.parseLong(number);
                if (longN >= Integer.MIN_VALUE && longN <= Integer.MAX_VALUE) {
                    return (int) longN;
                }
                return longN;
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Eror");
        }
    }

}
