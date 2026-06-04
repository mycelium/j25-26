package jsonlib.node;

import jsonlib.JsonParseException;
import java.math.BigDecimal;

public class JsonNumber extends JsonNode {
    private final String numberStr;

    public JsonNumber(String numberStr) {
        try {
            new BigDecimal(numberStr);
        } catch (NumberFormatException e) {
            throw new JsonParseException("Invalid number: " + numberStr);
        }
        this.numberStr = numberStr;
    }

    @Override public boolean isNumber() { return true; }

    public Number getValue() {
        if (numberStr.contains(".") || numberStr.contains("e") || numberStr.contains("E")) {
            return Double.parseDouble(numberStr);
        }
        try {
            return Long.parseLong(numberStr);
        } catch (NumberFormatException e) {
            return new BigDecimal(numberStr);
        }
    }

    public int intValue() { return getValue().intValue(); }
    public long longValue() { return getValue().longValue(); }
    public double doubleValue() { return getValue().doubleValue(); }
    public String getNumberString() { return numberStr; }

    @Override
    public String toJsonString() { return numberStr; }
}