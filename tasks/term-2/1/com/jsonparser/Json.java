package com.jsonparser;

import com.jsonparser.core.Reader;
import com.jsonparser.core.Writer;
import java.util.Map;

public final class Json {

    private Json() {}

    public static Object parse(String json) {
        return new Reader(json).read();
    }

    public static Map<String, Object> parseMap(String json) {
        return new Reader(json).readMap();
    }

    public static <T> T parseObject(String json, Class<T> type) {
        return new Reader(json).readObject(type);
    }

    public static String stringify(Object obj) {
        return new Writer().write(obj);
    }
}